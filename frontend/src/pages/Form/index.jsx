import { CheckCircle2, HeartHandshake, Home, PawPrint } from "lucide-react";
import { useEffect, useState } from "react";
import { atualizarPerfilAdotante, buscarMeuPerfil, COLD_START_MESSAGE, isColdStartError, solicitarAdocao } from "../../lib/api";
import { SectionHeading } from "../Home/shared";

export default function Form({ pet, session, onNavigate, onSubmitted }) {
  const [sent, setSent] = useState(false);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState(null);
  const [contact, setContact] = useState({ telefone: "", cidade: "" });

  useEffect(() => {
    if (!session) {
      return;
    }
    let active = true;

    async function loadProfile() {
      setLoading(true);
      setMessage("");
      try {
        const perfil = await buscarMeuPerfil(session.access_token);
        if (active) {
          setProfile(perfil);
          setContact({
            telefone: perfil.telefone || "",
            cidade: perfil.cidade || "",
          });
        }
      } catch (error) {
        if (active) {
          setMessage(isColdStartError(error) ? COLD_START_MESSAGE : error.message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadProfile();
    return () => {
      active = false;
    };
  }, [session]);

  const submitInterest = async () => {
    if (!session) {
      onNavigate("login");
      return;
    }
    if (!pet?.id) {
      setMessage("Selecione um pet antes de enviar a solicitação.");
      return;
    }
    setLoading(true);
    setMessage("");
    try {
      if (!contact.telefone.trim() || !contact.cidade.trim()) {
        setMessage("Informe telefone e cidade para continuar.");
        return;
      }
      if (profile) {
        await atualizarPerfilAdotante(
          {
            especies_preferidas: profile.especies_preferidas || [],
            preferencias: profile.preferencias || [],
            tipo_moradia: profile.tipo_moradia,
            espaco_disponivel: profile.espaco_disponivel,
            tempo_disponivel: profile.tempo_disponivel,
            experiencia_animais: profile.experiencia_animais,
            possui_criancas: profile.possui_criancas,
            possui_caes: profile.possui_caes,
            possui_gatos: profile.possui_gatos,
            telefone: contact.telefone,
            cidade: contact.cidade,
          },
          session.access_token
        );
      }
      await solicitarAdocao(pet.id, session.access_token);
      setSent(true);
      onSubmitted?.();
    } catch (error) {
      setMessage(isColdStartError(error) ? COLD_START_MESSAGE : error.message);
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <section className="form-page standalone-page">
        <div className="interest-success">
          <div className="interest-success-icon">
            <CheckCircle2 size={42} />
          </div>
          <span>Interesse enviado</span>
          <h1>Solicitação enviada com sucesso!</h1>
          <p>
            Agora a equipe responsável pelo pet recebeu sua solicitação e poderá continuar o processo de adoção com você.
          </p>
          <div className="interest-success-actions">
            <button className="primary-action" type="button" onClick={() => setSent(false)}>
              <HeartHandshake size={17} />
              Enviar outro interesse
            </button>
            <button className="secondary-action" type="button" onClick={() => onNavigate("home")}>
              <Home size={17} />
              Voltar para o início
            </button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="form-page standalone-page">
      <SectionHeading
        eyebrow="Cadastro"
        title="Formulário de adoção"
        text={pet ? `Confirme seu interesse em adotar ${pet.name}.` : "Escolha um pet antes de iniciar a solicitação."}
      />

      <form className="adoption-form" onSubmit={(event) => event.preventDefault()}>
        <div className="form-grid">
          <label>
            Nome completo
            <input value={session?.usuario?.nome || ""} readOnly placeholder="Seu nome" />
          </label>
          <label>
            E-mail
            <input value={session?.usuario?.email || ""} readOnly type="email" placeholder="exemplo@gmail.com" />
          </label>
          <label>
            Telefone
            <input value={contact.telefone} onChange={(event) => setContact((current) => ({ ...current, telefone: event.target.value }))} placeholder="(00) 00000-0000" />
          </label>
          <label>
            Cidade
            <input value={contact.cidade} onChange={(event) => setContact((current) => ({ ...current, cidade: event.target.value }))} placeholder="Sua cidade" />
          </label>
        </div>

        {profile && (
          <div className="adoption-profile-summary">
            <span>Perfil de adoção</span>
            <p>{profile.tipo_moradia || "Moradia não informada"} · {profile.espaco_disponivel || "espaço não informado"} · {profile.tempo_disponivel || "tempo não informado"}</p>
          </div>
        )}

        {message && <p className="form-message">{message}</p>}

        <button type="button" className="primary-action form-submit" onClick={submitInterest} disabled={loading}>
          <PawPrint size={17} />
          {loading ? "Enviando..." : "Enviar interesse"}
        </button>
      </form>
    </section>
  );
}
