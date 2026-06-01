import { useCallback, useEffect, useState } from "react";
import {
  ArrowRight,
  CheckCircle2,
  Heart,
  Home,
  MapPin,
  PawPrint,
  Sparkles,
} from "lucide-react";
import {
  atualizarPerfilAdotante,
  buscarMeuPerfil,
  COLD_START_MESSAGE,
  isColdStartError,
  listarRecomendacoes,
} from "../../lib/api";
import { mapAnimals, tagOptions } from "../../lib/pets";

const speciesOptions = [
  { value: "CACHORRO", label: "Cachorro" },
  { value: "GATO", label: "Gato" },
  { value: "OUTRO", label: "Outro" },
];

const selectOptions = {
  tipo_moradia: [
    { value: "APARTAMENTO", label: "Apartamento" },
    { value: "CASA", label: "Casa" },
    { value: "CASA_COM_QUINTAL", label: "Casa com quintal" },
    { value: "SITIO_CHACARA", label: "Sítio ou chácara" },
  ],
  espaco_disponivel: [
    { value: "PEQUENO", label: "Pequeno" },
    { value: "MEDIO", label: "Médio" },
    { value: "GRANDE", label: "Grande" },
  ],
  tempo_disponivel: [
    { value: "ATE_30_MIN", label: "Até 30 minutos" },
    { value: "UMA_HORA", label: "1 hora" },
    { value: "DUAS_HORAS_OU_MAIS", label: "2 horas ou mais" },
  ],
  experiencia_animais: [
    { value: "PRIMEIRA_ADOCAO", label: "Primeira adoção" },
    { value: "JA_TIVE_PETS", label: "Já tive pets" },
    { value: "TENHO_PETS_HOJE", label: "Tenho pets hoje" },
  ],
};

const defaultProfile = {
  especies_preferidas: ["GATO"],
  preferencias: ["CARINHOSO", "CALMO"],
  tipo_moradia: "APARTAMENTO",
  espaco_disponivel: "MEDIO",
  tempo_disponivel: "UMA_HORA",
  experiencia_animais: "JA_TIVE_PETS",
  possui_criancas: false,
  possui_caes: false,
  possui_gatos: false,
  telefone: "",
  cidade: "",
};

export default function Match({ session, onNavigate, onSelectPet }) {
  const [stage, setStage] = useState("profile");
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [profile, setProfile] = useState(defaultProfile);

  const loadMatches = useCallback(async () => {
    if (!session) {
      onNavigate("login");
      return;
    }
    const recomendacoes = await listarRecomendacoes(session.access_token);
    setMatches(mapAnimals(recomendacoes));
    setStage("results");
  }, [onNavigate, session]);

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
        if (!active) {
          return;
        }
        setProfile(toFormProfile(perfil));
        if (perfil.perfil_completo) {
          await loadMatches();
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
  }, [loadMatches, session]);

  const updateProfile = (name, value) => {
    setProfile((current) => ({ ...current, [name]: value }));
  };

  const toggleListValue = (name, value) => {
    setProfile((current) => {
      const values = current[name] || [];
      const nextValues = values.includes(value)
        ? values.filter((item) => item !== value)
        : [...values, value];
      return { ...current, [name]: nextValues };
    });
  };

  const saveProfileAndLoadMatches = async () => {
    if (!session) {
      onNavigate("login");
      return;
    }
    setLoading(true);
    setMessage("");
    try {
      const perfil = await atualizarPerfilAdotante(profile, session.access_token);
      setProfile(toFormProfile(perfil));
      await loadMatches();
    } catch (error) {
      setMessage(isColdStartError(error) ? COLD_START_MESSAGE : error.message);
    } finally {
      setLoading(false);
    }
  };

  if (stage === "results") {
    return (
      <MatchResults
        profile={profile}
        pets={matches}
        onNavigate={onNavigate}
        onSelectPet={onSelectPet}
        onRestart={() => setStage("profile")}
      />
    );
  }

  return (
    <section className="match-page standalone-page match-page-profile">
      <div className="match-intro">
        <div>
          <span>
            <Sparkles size={18} />
            Match MiAUmigos
          </span>
          <h1>Encontre seu novo melhor amigo</h1>
          <p>Seu perfil salva suas preferências para calcular compatibilidade com pets disponíveis.</p>
          <ul>
            <li>✔ Compatibilidade em porcentagem</li>
            <li>✔ Perfil salvo para os próximos matches</li>
            <li>✔ Tags de comportamento do pet em destaque</li>
          </ul>
          <p className="match-intro-note">💛 O começo de uma nova amizade.</p>
        </div>
      </div>

      <form className="match-form" onSubmit={(event) => event.preventDefault()}>
        <div className="section-heading">
          <span>Seu perfil de adoção</span>
          <h2>Complete seu perfil</h2>
          <p>Essas informações serão usadas nas recomendações e reaproveitadas na adoção.</p>
        </div>

        <fieldset className="auth-preferences">
          <legend>Espécies preferidas</legend>
          <div>
            {speciesOptions.map((option) => (
              <label key={option.value}>
                <input
                  type="checkbox"
                  checked={profile.especies_preferidas.includes(option.value)}
                  onChange={() => toggleListValue("especies_preferidas", option.value)}
                />
                {option.label}
              </label>
            ))}
          </div>
        </fieldset>

        <fieldset className="auth-preferences">
          <legend>Comportamentos preferidos</legend>
          <div>
            {tagOptions.map((option) => (
              <label key={option.value}>
                <input
                  type="checkbox"
                  checked={profile.preferencias.includes(option.value)}
                  onChange={() => toggleListValue("preferencias", option.value)}
                />
                {option.label}
              </label>
            ))}
          </div>
        </fieldset>

        <div className="match-question-grid">
          <SelectField label="Tipo de moradia" name="tipo_moradia" value={profile.tipo_moradia} onChange={updateProfile} />
          <SelectField label="Espaço disponível" name="espaco_disponivel" value={profile.espaco_disponivel} onChange={updateProfile} />
          <SelectField label="Tempo livre por dia" name="tempo_disponivel" value={profile.tempo_disponivel} onChange={updateProfile} />
          <SelectField label="Experiência com animais" name="experiencia_animais" value={profile.experiencia_animais} onChange={updateProfile} />
        </div>

        <div className="match-question-grid">
          <BooleanField label="Tenho crianças em casa" name="possui_criancas" checked={profile.possui_criancas} onChange={updateProfile} />
          <BooleanField label="Tenho cães em casa" name="possui_caes" checked={profile.possui_caes} onChange={updateProfile} />
          <BooleanField label="Tenho gatos em casa" name="possui_gatos" checked={profile.possui_gatos} onChange={updateProfile} />
        </div>

        {message && <p className="form-message">{message}</p>}
        <button className="primary-action match-submit" type="button" onClick={saveProfileAndLoadMatches} disabled={loading}>
          {loading ? "Buscando matches..." : "Salvar e ver pets compatíveis"}
          <ArrowRight size={17} />
        </button>
      </form>
    </section>
  );
}

function SelectField({ label, name, value, onChange }) {
  return (
    <label>
      {label}
      <select value={value} onChange={(event) => onChange(name, event.target.value)}>
        {selectOptions[name].map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}

function BooleanField({ label, name, checked, onChange }) {
  return (
    <label className="match-toggle-field">
      <input type="checkbox" checked={checked} onChange={(event) => onChange(name, event.target.checked)} />
      {label}
    </label>
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
          <p>Veja a porcentagem de compatibilidade e os comportamentos de cada pet.</p>
        </div>
        <button className="secondary-action" onClick={onRestart}>
          Ajustar perfil
          <ArrowRight size={17} />
        </button>
      </div>

      <div className="match-layout">
        <aside className="match-profile">
          <h2>Seu perfil</h2>
          <div>
            <span>
              <Home size={17} />
              {labelFor("tipo_moradia", profile.tipo_moradia)} · espaço {labelFor("espaco_disponivel", profile.espaco_disponivel).toLowerCase()}
            </span>
            <span>
              <MapPin size={17} />
              {profile.cidade || "Cidade não informada"}
            </span>
            <span>
              <Heart size={17} />
              {labelFor("tempo_disponivel", profile.tempo_disponivel)}
            </span>
            <span>
              <PawPrint size={17} />
              {labelFor("experiencia_animais", profile.experiencia_animais)}
            </span>
          </div>
          <button className="primary-action" onClick={() => onNavigate("adoption")}>
            Iniciar formulário
          </button>
        </aside>

        <div className="match-results">
          {pets.length === 0 && <p>Nenhum pet disponível para suas recomendações agora.</p>}
          {pets.map((pet) => (
            <article className="match-card" key={pet.id || pet.name}>
              <img src={pet.image} alt={pet.name} />
              <div className="match-card-body">
                <div className="match-score">
                  <strong>{pet.score}%</strong>
                  <span>compatibilidade</span>
                </div>
                <h3>{pet.name}</h3>
                <p>{pet.personality}</p>
                <div className="match-tags">
                  {pet.tags.map((tag) => (
                    <span key={tag}>{tag}</span>
                  ))}
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

function toFormProfile(perfil) {
  return {
    ...defaultProfile,
    especies_preferidas: perfil.especies_preferidas?.length ? perfil.especies_preferidas : defaultProfile.especies_preferidas,
    preferencias: perfil.preferencias?.length ? perfil.preferencias : defaultProfile.preferencias,
    tipo_moradia: perfil.tipo_moradia || defaultProfile.tipo_moradia,
    espaco_disponivel: perfil.espaco_disponivel || defaultProfile.espaco_disponivel,
    tempo_disponivel: perfil.tempo_disponivel || defaultProfile.tempo_disponivel,
    experiencia_animais: perfil.experiencia_animais || defaultProfile.experiencia_animais,
    possui_criancas: Boolean(perfil.possui_criancas),
    possui_caes: Boolean(perfil.possui_caes),
    possui_gatos: Boolean(perfil.possui_gatos),
    telefone: perfil.telefone || "",
    cidade: perfil.cidade || "",
  };
}

function labelFor(group, value) {
  return selectOptions[group].find((option) => option.value === value)?.label || "Não informado";
}
