const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "https://miaumigo.onrender.com").replace(/\/$/, "");

const SESSION_KEY = "miaumigo_session";

export function loadSession() {
	const rawSession = localStorage.getItem(SESSION_KEY);
	if (!rawSession) {
		return null;
	}
	try {
		return JSON.parse(rawSession);
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

async function request(path, options = {}) {
	const controller = new AbortController();
	const timeoutId = window.setTimeout(() => controller.abort(), 25000);
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
			throw new ApiError("A API demorou mais de 25 segundos para responder.", 0, null);
		}
		if (error instanceof TypeError) {
			throw new ApiError("Não foi possível conectar à API. Verifique se o backend está no ar e se o CORS permite este frontend.", 0, null);
		}
		throw error;
	} finally {
		window.clearTimeout(timeoutId);
	}
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
	constructor(message, status, data) {
		super(message);
		this.name = "ApiError";
		this.status = status;
		this.data = data;
	}
}
