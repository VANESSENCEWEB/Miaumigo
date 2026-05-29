import { CheckCircle2, HeartHandshake, Home, PawPrint } from "lucide-react";
import { useState } from "react";
import { solicitarAdocao } from "../../lib/api";
import { SectionHeading } from "../Home/shared";

export default function Form({ pet, session, onNavigate, onSubmitted }) {
  const [sent, setSent] = useState(false);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

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
      await solicitarAdocao(pet.id, session.access_token);
      setSent(true);
      onSubmitted?.();
    } catch (error) {
      setMessage(error.message);
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
            <input placeholder="(00) 00000-0000" />
          </label>
          <label>
            Cidade
            <input placeholder="Sua cidade" />
          </label>
          <label>
            Tipo de moradia
            <select defaultValue="">
              <option value="" disabled>
                Selecione
              </option>
              <option>Casa</option>
              <option>Apartamento</option>
              <option>Outro</option>
            </select>
          </label>
          <label>
            Qual tipo de pet você procura?
            <select defaultValue="">
              <option value="" disabled>
                Selecione
              </option>
              <option>Cachorro</option>
              <option>Gato</option>
              <option>Filhote</option>
              <option>Ainda estou escolhendo</option>
            </select>
          </label>
        </div>

        <label>
          Conte sobre sua rotina
          <textarea placeholder="Horários, pessoas na casa, outros pets e experiência anterior." />
        </label>

        {message && <p className="form-message">{message}</p>}

        <button type="button" className="primary-action form-submit" onClick={submitInterest} disabled={loading}>
          <PawPrint size={17} />
          {loading ? "Enviando..." : "Enviar interesse"}
        </button>
      </form>
    </section>
  );
}
