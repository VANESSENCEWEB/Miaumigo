import { ArrowRight, CheckCircle2, Headphones, Lock, Mail, MessageCircle, ShieldCheck } from "lucide-react";
import { useState } from "react";
import { COLD_START_MESSAGE, enviarMensagemSuporte, getAdotanteSession, isColdStartError } from "../../lib/api";
import { SectionHeading } from "../Home/shared";

const initialForm = {
  assunto: "",
  mensagem: "",
};

export default function Suporte({ session, onNavigate }) {
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [success, setSuccess] = useState(false);
  const adotanteSession = getAdotanteSession(session);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setMessage("");
    setSuccess(false);
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!adotanteSession) {
      onNavigate("login");
      return;
    }
    setLoading(true);
    setMessage("");
    setSuccess(false);
    try {
      await enviarMensagemSuporte(
        {
          assunto: form.assunto,
          mensagem: form.mensagem,
        },
        adotanteSession.access_token
      );
      setForm(initialForm);
      setSuccess(true);
    } catch (error) {
      setMessage(isColdStartError(error) ? COLD_START_MESSAGE : error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="support-page form-page standalone-page">
      <SectionHeading
        eyebrow="Suporte"
        title="Fale com o suporte"
        text="Envie uma mensagem para a equipe sobre dúvidas do processo de adoção."
      />

      <div className="support-layout">
        <aside className="support-aside">
          <span>
            <Headphones size={18} />
            Atendimento ao adotante
          </span>
          <h2>Conte o que está acontecendo</h2>
          <p>A mensagem ficará registrada para acompanhamento interno. Nesta versão ela ainda não é direcionada a um lar temporário específico.</p>
          <div>
            <span>
              <ShieldCheck size={17} />
              Envio seguro com sua conta
            </span>
            <span>
              <Lock size={17} />
              Acesso exclusivo para adotantes
            </span>
            <span>
              <Mail size={17} />
              Registro interno da solicitação
            </span>
          </div>
        </aside>

        <form className="support-form adoption-form" onSubmit={submit}>
          <label>
            Assunto
            <input
              value={form.assunto}
              maxLength={120}
              onChange={(event) => updateField("assunto", event.target.value)}
              placeholder="Ex.: Dúvida sobre o processo de adoção"
              required
            />
          </label>

          <label>
            Mensagem
            <textarea
              value={form.mensagem}
              minLength={10}
              maxLength={1000}
              rows={7}
              onChange={(event) => updateField("mensagem", event.target.value)}
              placeholder="Descreva sua dúvida ou problema com o máximo de contexto possível."
              required
            />
          </label>

          {message && <p className="form-message">{message}</p>}
          {success && (
            <p className="support-success">
              <CheckCircle2 size={18} />
              Mensagem enviada com sucesso.
            </p>
          )}

          <button type="submit" className="primary-action form-submit" disabled={loading}>
            <MessageCircle size={17} />
            {loading ? "Enviando..." : "Enviar mensagem"}
            <ArrowRight size={17} />
          </button>
        </form>
      </div>
    </section>
  );
}
