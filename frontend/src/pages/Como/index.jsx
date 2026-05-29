import {
  ClipboardList,
  Heart,
  MessageCircle,
  SearchCheck,
  ShieldCheck,
  Sparkles,
} from "lucide-react";
import { SectionHeading } from "../Home/shared";

export default function Como({ onNavigate, homeBlock = false }) {
  const steps = [
    {
      title: "Perfil seguro",
      text: "Login ou cadastro organiza seus dados antes do match.",
      icon: ShieldCheck,
    },
    {
      title: "Busca com carinho",
      text: "Encontre pets por rotina, cidade, porte e personalidade.",
      icon: SearchCheck,
    },
    {
      title: "Match inteligente",
      text: "O sistema cruza seu perfil com animais que combinam com você.",
      icon: Sparkles,
    },
    {
      title: "Conversa guiada",
      text: "Depois do interesse, você segue para o formulário e contato com a ONG.",
      icon: MessageCircle,
    },
  ];

  return (
    <section className={homeBlock ? "content-section soft-band how-page" : "content-section standalone-page how-page"}>
      <SectionHeading
        eyebrow="Como funciona?"
        title="Adoção responsável"
        text="A jornada começa com uma conta, passa pelo match e termina com uma conversa mais segura entre adotante e protetor."
      />

      <div className="steps-grid">
        {steps.map(({ title, text, icon: Icon }, index) => (
          <article className="step-card" key={title}>
            <div className="step-icon">
              <Icon size={23} />
            </div>
            <strong>{String(index + 1).padStart(2, "0")}</strong>
            <h3>{title}</h3>
            <p>{text}</p>
          </article>
        ))}
      </div>

      <div className="match-callout">
        <div>
          <h3>Encontre pets compatíveis com você</h3>
          <p>
           Responda algumas perguntas e descubra animais que combinam com sua rotina.
          </p>
        </div>
        <button className="primary-action" onClick={() => onNavigate("login")}>
          <Heart size={17} fill="currentColor" />
          Fazer meu Match
        </button>
      </div>

      <button className="secondary-action section-action" onClick={() => onNavigate("adoption")}>
        <ClipboardList size={17} />
        Começar cadastro
      </button>
    </section>
  );
}
