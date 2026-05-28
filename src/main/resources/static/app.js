const tags = [
	"CALMO",
	"CARINHOSO",
	"BRINCALHAO",
	"INDEPENDENTE",
	"SOCIAL",
	"ENERGICO",
	"PROTETOR",
	"ADAPTADO_A_APARTAMENTO",
	"PRECISA_DE_ESPACO",
	"CONVIVE_COM_CRIANCAS",
	"CONVIVE_COM_CAES",
	"CONVIVE_COM_GATOS"
];

const state = {
	token: localStorage.getItem("miaumigo_token") || "",
	user: JSON.parse(localStorage.getItem("miaumigo_user") || "null")
};

const elements = {
	apiBase: document.querySelector("#apiBase"),
	apiLog: document.querySelector("#apiLog"),
	toast: document.querySelector("#toast"),
	sessionName: document.querySelector("#sessionName"),
	sessionRole: document.querySelector("#sessionRole"),
	preferenciasCadastro: document.querySelector("#preferenciasCadastro"),
	recommendationsList: document.querySelector("#recommendationsList"),
	requestsList: document.querySelector("#requestsList")
};

function init() {
	elements.apiBase.value = initialApiBase();
	renderTagChoices();
	renderSession();
	bindNavigation();
	bindForms();
}

function initialApiBase() {
	const queryApi = new URLSearchParams(window.location.search).get("api");
	if (queryApi) {
		localStorage.setItem("miaumigo_api", queryApi);
		return queryApi;
	}
	const savedApi = localStorage.getItem("miaumigo_api");
	if (savedApi && !isLocalFrontend()) {
		return savedApi;
	}
	if (isLocalFrontend()) {
		return window.location.origin;
	}
	return savedApi || elements.apiBase.value;
}

function isLocalFrontend() {
	return ["localhost", "127.0.0.1"].includes(window.location.hostname);
}

function bindNavigation() {
	document.querySelectorAll(".step").forEach((button) => {
		button.addEventListener("click", () => activatePanel(button.dataset.panel));
	});
	document.querySelector("#clearLog").addEventListener("click", () => {
		elements.apiLog.textContent = "Log limpo.";
	});
	document.querySelector("#logoutButton").addEventListener("click", logout);
	document.querySelector("#loadRecommendations").addEventListener("click", loadRecommendations);
	document.querySelector("#loadRequests").addEventListener("click", loadRequests);
	elements.apiBase.addEventListener("change", () => {
		localStorage.setItem("miaumigo_api", apiBase());
	});
}

function bindForms() {
	document.querySelector("#cadastroForm").addEventListener("submit", async (event) => {
		event.preventDefault();
		const form = new FormData(event.currentTarget);
		const payload = {
			nome: form.get("nome"),
			endereco: form.get("endereco"),
			email: form.get("email"),
			senha: form.get("senha"),
			cpf: form.get("cpf"),
			preferencias: selectedTags()
		};
		await request("/api/v1/adotantes", {
			method: "POST",
			body: payload,
			onSuccess: () => {
				toast("Adotante cadastrado. Faça login para continuar.");
				activatePanel("login");
				document.querySelector("#loginForm [name='email']").value = payload.email;
				document.querySelector("#loginForm [name='senha']").value = payload.senha;
			}
		});
	});

	document.querySelector("#loginForm").addEventListener("submit", async (event) => {
		event.preventDefault();
		const form = new FormData(event.currentTarget);
		await request("/api/v1/auth/login", {
			method: "POST",
			body: {
				email: form.get("email"),
				senha: form.get("senha")
			},
			onSuccess: (data) => {
				state.token = data.access_token;
				state.user = data.usuario;
				localStorage.setItem("miaumigo_token", state.token);
				localStorage.setItem("miaumigo_user", JSON.stringify(state.user));
				renderSession();
				toast("Login realizado.");
				activatePanel("recomendacoes");
			}
		});
	});
}

function renderTagChoices() {
	elements.preferenciasCadastro.innerHTML = tags.map((tag, index) => `
		<label class="choice">
			<input type="checkbox" value="${tag}" ${index < 3 ? "checked" : ""}>
			${formatEnum(tag)}
		</label>
	`).join("");
}

function selectedTags() {
	return [...elements.preferenciasCadastro.querySelectorAll("input:checked")]
		.map((input) => input.value);
}

async function loadRecommendations() {
	await request("/api/v1/adotantes/me/animais-recomendados", {
		method: "GET",
		auth: true,
		onSuccess: (data) => {
			renderRecommendations(data);
			toast(`${data.length} recomendação(ões) carregada(s).`);
		}
	});
}

async function loadRequests() {
	await request("/api/v1/adotantes/me/solicitacoes", {
		method: "GET",
		auth: true,
		onSuccess: (data) => {
			renderRequests(data);
			toast("Status atualizado.");
		}
	});
}

async function createAdoptionRequest(animalId) {
	await request(`/api/v1/animais/${animalId}/solicitacoes`, {
		method: "POST",
		auth: true,
		onSuccess: () => {
			toast("Solicitação enviada.");
			loadRequests();
			activatePanel("solicitacoes");
		}
	});
}

async function cancelRequest(requestId) {
	await request(`/api/v1/solicitacoes/${requestId}/cancelamento`, {
		method: "POST",
		auth: true,
		onSuccess: () => {
			toast("Solicitação cancelada.");
			loadRequests();
		}
	});
}

function renderRecommendations(items) {
	if (!items.length) {
		elements.recommendationsList.className = "cards empty-state";
		elements.recommendationsList.textContent = "Nenhum animal disponível retornado pela API.";
		return;
	}
	elements.recommendationsList.className = "cards";
	elements.recommendationsList.innerHTML = items.map((animal) => `
		<article class="animal-card">
			<img alt="" src="${animalImage(animal)}">
			<div class="content">
				<div>
					<h3>${escapeHtml(animal.nome || "Sem nome")}</h3>
					<p class="meta">${formatEnum(animal.especie)} · ${formatEnum(animal.porte)} · ${animal.idade ?? "-"} ano(s)</p>
				</div>
				<strong>Compatibilidade: ${animal.compatibilidade}</strong>
				<div class="tags">${(animal.tags || []).map((tag) => `<span class="tag">${formatEnum(tag)}</span>`).join("")}</div>
				<button type="button" data-adopt="${animal.id}">Solicitar adoção</button>
			</div>
		</article>
	`).join("");
	elements.recommendationsList.querySelectorAll("[data-adopt]").forEach((button) => {
		button.addEventListener("click", () => createAdoptionRequest(button.dataset.adopt));
	});
}

function renderRequests(items) {
	if (!items.length) {
		elements.requestsList.className = "table-wrap empty-state";
		elements.requestsList.textContent = "Nenhuma solicitação encontrada para este adotante.";
		return;
	}
	elements.requestsList.className = "table-wrap";
	elements.requestsList.innerHTML = `
		<table>
			<thead>
				<tr>
					<th>Animal</th>
					<th>Status</th>
					<th>Criado em</th>
					<th>Ação</th>
				</tr>
			</thead>
			<tbody>
				${items.map((item) => `
					<tr>
						<td>${escapeHtml(item.animal_nome || "-")}</td>
						<td><span class="status ${item.status}">${formatEnum(item.status)}</span></td>
						<td>${formatDate(item.criado_em)}</td>
						<td>
							${item.status === "PENDENTE"
								? `<button class="ghost" type="button" data-cancel="${item.id}">Cancelar</button>`
								: `<span class="meta">Sem ação</span>`}
						</td>
					</tr>
				`).join("")}
			</tbody>
		</table>
	`;
	elements.requestsList.querySelectorAll("[data-cancel]").forEach((button) => {
		button.addEventListener("click", () => cancelRequest(button.dataset.cancel));
	});
}

async function request(path, options) {
	const headers = { "Accept": "application/json" };
	if (options.body !== undefined) {
		headers["Content-Type"] = "application/json";
	}
	if (options.auth) {
		if (!state.token) {
			toast("Faça login antes de continuar.");
			activatePanel("login");
			return;
		}
		headers.Authorization = `Bearer ${state.token}`;
	}

	const url = `${apiBase()}${path}`;
	const controller = new AbortController();
	const timeout = window.setTimeout(() => controller.abort(), 25000);
	setBusy(true);
	try {
		const response = await fetch(url, {
			method: options.method,
			headers,
			body: options.body === undefined ? undefined : JSON.stringify(options.body),
			signal: controller.signal
		});
		const text = await response.text();
		const data = text ? safeJson(text) : null;
		logResponse(options.method, url, response.status, data ?? text);
		if (!response.ok) {
			toast(errorMessage(data, response.status));
			return;
		}
		options.onSuccess?.(data);
	} catch (error) {
		const timeoutMessage = error.name === "AbortError"
				? "A API demorou mais de 25 segundos para responder. No Render, isso pode acontecer quando o serviço está acordando."
				: null;
		const corsHint = apiBase().includes("miaumigo.onrender.com") && isLocalFrontend()
				? "Você está em localhost chamando a API do Render. Se o Render ainda não tiver a configuração CORS publicada, o navegador bloqueia a chamada. Para testar localmente agora, use http://localhost:8080 no campo API."
				: null;
		elements.apiLog.textContent = [
			"Falha ao chamar a API.",
			"",
			timeoutMessage,
			timeoutMessage ? "" : null,
			corsHint,
			corsHint ? "" : null,
			"Possíveis causas:",
			"- API fora do ar ou dormindo no Render.",
			"- CORS bloqueando chamada do navegador.",
			"- URL da API incorreta.",
			"",
			String(error)
		].filter((line) => line !== null).join("\n");
		toast("Não foi possível chamar a API.");
	} finally {
		window.clearTimeout(timeout);
		setBusy(false);
	}
}

function safeJson(text) {
	try {
		return JSON.parse(text);
	} catch {
		return text;
	}
}

function logResponse(method, url, status, data) {
	elements.apiLog.textContent = `${method} ${url}\nStatus: ${status}\n\n${JSON.stringify(data, null, 2)}`;
}

function errorMessage(data, status) {
	if (data && typeof data === "object" && data.mensagem) {
		return data.mensagem;
	}
	return `Erro ${status} na API.`;
}

function apiBase() {
	return elements.apiBase.value.replace(/\/+$/, "");
}

function activatePanel(id) {
	document.querySelectorAll(".panel").forEach((panel) => {
		panel.classList.toggle("is-active", panel.id === id);
	});
	document.querySelectorAll(".step").forEach((button) => {
		button.classList.toggle("is-active", button.dataset.panel === id);
	});
}

function renderSession() {
	elements.sessionName.textContent = state.user?.nome || "Sem login";
	elements.sessionRole.textContent = state.user?.papel || "-";
}

function logout() {
	state.token = "";
	state.user = null;
	localStorage.removeItem("miaumigo_token");
	localStorage.removeItem("miaumigo_user");
	renderSession();
	toast("Sessão encerrada.");
	activatePanel("login");
}

function setBusy(isBusy) {
	document.querySelectorAll("button").forEach((button) => {
		button.disabled = isBusy;
	});
}

function toast(message) {
	elements.toast.textContent = message;
	elements.toast.classList.add("is-visible");
	window.clearTimeout(toast.timeout);
	toast.timeout = window.setTimeout(() => {
		elements.toast.classList.remove("is-visible");
	}, 2800);
}

function formatEnum(value) {
	if (!value) {
		return "-";
	}
	return String(value).toLowerCase().replaceAll("_", " ");
}

function formatDate(value) {
	if (!value) {
		return "-";
	}
	return new Intl.DateTimeFormat("pt-BR", {
		dateStyle: "short",
		timeStyle: "short"
	}).format(new Date(value));
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#039;");
}

function animalImage(animal) {
	const especie = String(animal.especie || "").toUpperCase();
	if (especie === "GATO") {
		return "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80";
	}
	if (especie === "CACHORRO") {
		return "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=600&q=80";
	}
	return "https://images.unsplash.com/photo-1450778869180-41d0601e046e?auto=format&fit=crop&w=600&q=80";
}

init();
