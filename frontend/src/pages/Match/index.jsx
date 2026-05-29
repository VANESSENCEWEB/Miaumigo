import { useState } from "react";
import {
  ArrowRight,
  CheckCircle2,
  Heart,
  Home,
  MapPin,
  PawPrint,
  Sparkles,
} from "lucide-react";
import { listarRecomendacoes } from "../../lib/api";
import { mapAnimals } from "../../lib/pets";

const questions = [
  {
    label: "Tipo de moradia",
    name: "homeType",
    options: ["Apartamento", "Casa", "Casa com quintal", "Sítio ou chácara"],
  },
  {
    label: "Espaço disponível",
    name: "space",
    options: ["Pequeno", "Médio", "Grande"],
  },
  {
    label: "Tempo livre por dia",
    name: "time",
    options: ["Até 30 minutos", "1 hora", "2 horas ou mais"],
  },
  {
    label: "Personalidade que combina com você",
    name: "personality",
    options: ["Calmo", "Brincalhão", "Independente", "Carinhoso"],
  },
  {
    label: "Experiência com animais",
    name: "experience",
    options: ["Primeira adoção", "Já tive pets", "Tenho pets hoje"],
  },
];

export default function Match({ session, onNavigate, onSelectPet }) {
  const [stage, setStage] = useState("profile");
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [profile, setProfile] = useState({
    homeType: "Apartamento",
    space: "Médio",
    time: "1 hora",
    personality: "Carinhoso",
    experience: "Já tive pets",
  });

  const updateProfile = (name, value) => {
    setProfile((current) => ({ ...current, [name]: value }));
  };

  const loadMatches = async () => {
    if (!session) {
      onNavigate("login");
      return;
    }
    setLoading(true);
    setMessage("");
    try {
      const recomendacoes = await listarRecomendacoes(session.access_token);
      setMatches(mapAnimals(recomendacoes));
      setStage("results");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  if (stage === "results") {
    return <MatchResults profile={profile} pets={matches} onNavigate={onNavigate} onSelectPet={onSelectPet} onRestart={() => setStage("profile")} />;
  }

  return (
    <section className={`match-page standalone-page match-page-${stage}`}>
      <div className="match-intro">
        <div>
          <span>
            <Sparkles size={18} />
            Match MiAUmigos
          </span>
          <h1>Encontre seu novo melhor amigo</h1>
          <p>
            Responda algumas perguntas e descubra pets que combinam com sua rotina e seu jeito de viver.
          </p>
          <ul>
            <li>✔ Compatibilidade com sua rotina</li>
            <li>✔ Pets ideais para seu espaço</li>
            <li>✔ Recomendações personalizadas</li>
            <li>✔ Matches mais compatíveis com você</li>
            <li>✔ Processo simples e acolhedor</li>
          </ul>
          <p className="match-intro-note">💛 O começo de uma nova amizade.</p>
          {stage === "intro" && (
            <button className="primary-action" onClick={() => setStage("profile")}>
              <Heart size={17} fill="currentColor" />
              Iniciar Match
            </button>
          )}
        </div>
      </div>

      {stage === "profile" && (
        <form className="match-form" onSubmit={(event) => event.preventDefault()}>
          <div className="section-heading">
            <span>Seu perfil de adoção</span>
            <h2>Monte seu perfil de adoção</h2>
            <p>Conte sobre sua rotina e preferências para recomendarmos pets compatíveis com você.</p>
          </div>

          <div className="match-question-grid">
            {questions.map((question) => (
              <label key={question.name}>
                {question.label}
                <select
                  value={profile[question.name]}
                  onChange={(event) => updateProfile(question.name, event.target.value)}
                >
                  {question.options.map((option) => (
                    <option key={option}>{option}</option>
                  ))}
                </select>
              </label>
            ))}
          </div>

          {message && <p className="form-message">{message}</p>}
          <button className="primary-action match-submit" type="button" onClick={loadMatches} disabled={loading}>
            {loading ? "Buscando matches..." : "Ver pets compatíveis"}
            <ArrowRight size={17} />
          </button>
        </form>
      )}
    </section>
  );
}

function MatchResults({ profile, pets, onNavigate, onSelectPet, onRestart }) {
  return (
    <section className="match-page standalone-page">
      <div className="match-hero">
        <div>
          <span>
            <Sparkles size={17} />
            Resultado do match
          </span>
          <h1>Pets compatíveis com você</h1>
          <p>
            Cruzamos moradia, espaço, tempo livre, personalidade e experiência
            para sugerir pets com mais chance de adaptação.
          </p>
        </div>
        <button className="secondary-action" onClick={onRestart}>
          Ajustar respostas
          <ArrowRight size={17} />
        </button>
      </div>

      <div className="match-layout">
        <aside className="match-profile">
          <h2>Seu perfil</h2>
          <div>
            <span>
              <Home size={17} />
              {profile.homeType} · espaço {profile.space.toLowerCase()}
            </span>
            <span>
              <MapPin size={17} />
              Recife e região
            </span>
            <span>
              <Heart size={17} />
              {profile.personality} · {profile.time}
            </span>
            <span>
              <PawPrint size={17} />
              {profile.experience}
            </span>
          </div>
          <button className="primary-action" onClick={() => onNavigate("adoption")}>
            Iniciar formulário
          </button>
        </aside>

        <div className="match-results">
          {pets.length === 0 && <p>Nenhum pet disponível para suas recomendações agora.</p>}
          {pets.map((pet) => (
            <article className="match-card" key={pet.name}>
              <img src={pet.image} alt={pet.name} />
              <div className="match-card-body">
                <div className="match-score">
                  <strong>{pet.score}</strong>
                  <span>pontos de compatibilidade</span>
                </div>
                <h3>{pet.name}</h3>
                <p>{pet.personality}</p>
                <div className="match-tags">
                  <span>{pet.city}</span>
                  <span>{pet.age}</span>
                  <span>{pet.size}</span>
                </div>
                <button className="secondary-action" onClick={() => onSelectPet(pet)}>
                  <CheckCircle2 size={17} />
                  Quero conhecer
                </button>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
