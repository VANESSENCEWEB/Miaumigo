const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "https://miaumigo.onrender.com").replace(/\/$/, "");
const REQUEST_TIMEOUT_MS = 25000;
const COLD_START_CODE = "COLD_START_TIMEOUT";
export const COLD_START_MESSAGE = "Carregando aplicação. A primeira resposta pode demorar enquanto o servidor inicia.";

const SESSION_KEY = "miaumigo_session";
const PAPEL_ADOTANTE = "ADOTANTE";

export function loadSession() {
	const rawSession = localStorage.getItem(SESSION_KEY);
	if (!rawSession) {
		return null;
	}
	try {
		const session = JSON.parse(rawSession);
		if (!hasValidToken(session) || isSessionExpired(session)) {
			localStorage.removeItem(SESSION_KEY);
			return null;
		}
		return session;
	} catch {
		localStorage.removeItem(SESSION_KEY);
		return null;
	}
}

export function saveSession(session) {
	localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
	localStorage.removeItem(SESSION_KEY);
}

export function getAdotanteSession(currentSession = null) {
	const session = hasValidToken(currentSession) ? currentSession : loadSession();
	if (!session) {
		return null;
	}
	if (isSessionExpired(session)) {
		clearSession();
		return null;
	}
	return isAdotanteSession(session) ? session : null;
}

export function isAdotanteSession(session) {
	return hasValidToken(session) && session.usuario?.papel === PAPEL_ADOTANTE;
}

export async function login(credentials) {
	return request("/api/v1/auth/login", {
		method: "POST",
		body: credentials,
	});
}

export async function cadastrarAdotante(payload) {
	return request("/api/v1/adotantes", {
		method: "POST",
		body: payload,
	});
}

export async function listarAnimaisDisponiveis() {
	return request("/api/v1/animais?status=DISPONIVEL");
}

export async function buscarAnimal(id) {
	return request(`/api/v1/animais/${id}`);
}

export async function listarRecomendacoes(token) {
	return request("/api/v1/adotantes/me/animais-recomendados", {
		token,
	});
}

export async function buscarMeuPerfil(token) {
	return request("/api/v1/adotantes/me", {
		token,
	});
}

export async function atualizarPerfilAdotante(payload, token) {
	return request("/api/v1/adotantes/me/perfil", {
		method: "PATCH",
		body: payload,
		token,
	});
}

export async function solicitarAdocao(animalId, token) {
	return request(`/api/v1/animais/${animalId}/solicitacoes`, {
		method: "POST",
		token,
	});
}

export async function listarSolicitacoes(token) {
	return request("/api/v1/adotantes/me/solicitacoes", {
		token,
	});
}

export async function cancelarSolicitacao(id, token) {
	return request(`/api/v1/solicitacoes/${id}/cancelamento`, {
		method: "POST",
		token,
	});
}

export async function enviarMensagemSuporte(payload, token) {
	return request("/api/v1/adotantes/me/suporte", {
		method: "POST",
		body: payload,
		token,
	});
}

async function request(path, options = {}) {
	const controller = new AbortController();
	const timeoutId = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
	const headers = {
		Accept: "application/json",
		...options.headers,
	};
	if (options.body !== undefined) {
		headers["Content-Type"] = "application/json";
	}
	if (options.token) {
		headers.Authorization = `Bearer ${options.token}`;
	}

	try {
		const response = await fetch(`${API_BASE_URL}${path}`, {
			method: options.method || "GET",
			headers,
			body: options.body === undefined ? undefined : JSON.stringify(options.body),
			signal: controller.signal,
		});
		const text = await response.text();
		const data = text ? parseJson(text) : null;

		if (!response.ok) {
			throw new ApiError(errorMessage(data, response.status), response.status, data);
		}
		return data;
	} catch (error) {
		if (error.name === "AbortError") {
			throw new ApiError(COLD_START_MESSAGE, 0, null, COLD_START_CODE);
		}
		if (error instanceof TypeError) {
			throw new ApiError("Não foi possível conectar à API. Verifique se o backend está no ar e se o CORS permite este frontend.", 0, null);
		}
		throw error;
	} finally {
		window.clearTimeout(timeoutId);
	}
}

export function isColdStartError(error) {
	return error instanceof ApiError && error.code === COLD_START_CODE;
}

function parseJson(text) {
	try {
		return JSON.parse(text);
	} catch {
		return text;
	}
}

function errorMessage(data, status) {
	if (data?.mensagem) {
		return data.erros?.length ? `${data.mensagem}: ${data.erros.join(", ")}` : data.mensagem;
	}
	return `A API respondeu com status ${status}.`;
}

export class ApiError extends Error {
	constructor(message, status, data, code = null) {
		super(message);
		this.name = "ApiError";
		this.status = status;
		this.data = data;
		this.code = code;
	}
}

function hasValidToken(session) {
	return Boolean(session?.access_token);
}

function isSessionExpired(session) {
	if (!session?.expira_em) {
		return false;
	}
	const expiration = Date.parse(session.expira_em);
	if (Number.isNaN(expiration)) {
		return false;
	}
	return expiration <= Date.now();
}
