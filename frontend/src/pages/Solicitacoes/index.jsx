import { useCallback, useEffect, useState } from "react";
import { CheckCircle2, Clock, Home, XCircle } from "lucide-react";
import { cancelarSolicitacao, listarSolicitacoes } from "../../lib/api";
import { SectionHeading } from "../Home/shared";

const statusLabels = {
	PENDENTE: "Pendente",
	APROVADA: "Aprovada",
	REJEITADA: "Rejeitada",
	CANCELADA: "Cancelada",
};

export default function Solicitacoes({ session, onNavigate }) {
	const [requests, setRequests] = useState([]);
	const [loading, setLoading] = useState(false);
	const [message, setMessage] = useState("");

	const loadRequests = useCallback(async () => {
		setLoading(true);
		setMessage("");
		try {
			setRequests(await listarSolicitacoes(session.access_token));
		} catch (error) {
			setMessage(error.message);
		} finally {
			setLoading(false);
		}
	}, [session]);

	useEffect(() => {
		if (!session) {
			onNavigate("login");
			return;
		}
		const timeoutId = window.setTimeout(() => {
			loadRequests();
		}, 0);
		return () => window.clearTimeout(timeoutId);
	}, [loadRequests, onNavigate, session]);

	const cancel = async (id) => {
		setLoading(true);
		setMessage("");
		try {
			await cancelarSolicitacao(id, session.access_token);
			await loadRequests();
		} catch (error) {
			setMessage(error.message);
			setLoading(false);
		}
	};

	return (
		<section className="form-page standalone-page">
			<SectionHeading
				eyebrow="Acompanhamento"
				title="Minhas solicitações"
				text="Veja o andamento dos seus interesses de adoção."
			/>

			<div className="requests-list">
				{message && <p className="form-message">{message}</p>}
				{loading && <p>Carregando solicitações...</p>}
				{!loading && requests.length === 0 && (
					<div className="interest-success">
						<div className="interest-success-icon">
							<Home size={34} />
						</div>
						<span>Nenhuma solicitação</span>
						<h1>Você ainda não enviou interesses.</h1>
						<button className="primary-action" type="button" onClick={() => onNavigate("pets")}>
							Encontrar pets
						</button>
					</div>
				)}
				{requests.map((request) => (
					<article className="request-card" key={request.id}>
						<div>
							<span>{request.criado_em}</span>
							<h3>{request.animal_nome}</h3>
							<p>Status: {statusLabels[request.status] || request.status}</p>
						</div>
						{request.status === "PENDENTE" ? (
							<button className="secondary-action" type="button" onClick={() => cancel(request.id)} disabled={loading}>
								<XCircle size={17} />
								Cancelar
							</button>
						) : (
							<span className="request-status">
								{request.status === "APROVADA" ? <CheckCircle2 size={17} /> : <Clock size={17} />}
								{statusLabels[request.status] || request.status}
							</span>
						)}
					</article>
				))}
			</div>
		</section>
	);
}
