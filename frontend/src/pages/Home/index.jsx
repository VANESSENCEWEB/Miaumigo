import "./style.css";
import { useEffect, useState } from "react";
import {
  Bell,
  CalendarCheck,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  HandHeart,
  Heart,
  HelpCircle,
  Home as HomeIcon,
  Menu,
  MessageCircle,
  PawPrint,
  ShieldCheck,
  UserCircle,
  Users,
  X,
} from "lucide-react";
import Ajuda from "../Ajuda";
import Como from "../Como";
import Encontrar from "../Encontrar";
import Form from "../Form";
import Historias from "../Historias";
import LoginCadastro from "../login/cadastro";
import Match from "../Match";
import Ongs from "../Ongs";
import SobrePets from "../SobrePets";
import Solicitacoes from "../Solicitacoes";
import Suporte from "../Suporte";
import { COLD_START_MESSAGE, buscarAnimal, clearSession, getAdotanteSession, isColdStartError, listarAnimaisDisponiveis, loadSession, saveSession } from "../../lib/api";
import { mapAnimal, mapAnimals } from "../../lib/pets";
import { helpOptions, orgs, petCategories, stories } from "./data";
import { PetCard } from "./shared";

const trustBadges = ["100% gratuito", "Adoção segura", "ONGs verificadas"];

export default function Home() {
  const [activePage, setActivePage] = useState("home");
  const [menuOpen, setMenuOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedPet, setSelectedPet] = useState(null);
  const [catalogPets, setCatalogPets] = useState([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [apiError, setApiError] = useState("");
  const [appLoadingMessage, setAppLoadingMessage] = useState("");
  const [session, setSession] = useState(() => loadSession());

  useEffect(() => {
    let active = true;
    let retryId = null;

    async function loadCatalog() {
      setCatalogLoading(true);
      setApiError("");
      try {
        const animais = await listarAnimaisDisponiveis();
        if (active) {
          setCatalogPets(mapAnimals(animais));
          setAppLoadingMessage("");
          setCatalogLoading(false);
        }
      } catch (error) {
        if (active) {
          setCatalogPets([]);
          if (isColdStartError(error)) {
            setAppLoadingMessage(COLD_START_MESSAGE);
            retryId = window.setTimeout(loadCatalog, 5000);
            return;
          }
          setAppLoadingMessage("");
          setApiError(error.message);
          setCatalogLoading(false);
        }
      }
    }

    loadCatalog();
    return () => {
      active = false;
      if (retryId) {
        window.clearTimeout(retryId);
      }
    };
  }, []);

  const navigate = (page) => {
    setActivePage(page);
    setMenuOpen(false);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSession = (nextSession) => {
    setSession(nextSession);
    saveSession(nextSession);
  };

  const logout = () => {
    clearSession();
    setSession(null);
    navigate("home");
  };

  const goToMatch = () => {
    const adotanteSession = getAdotanteSession(session);
    if (adotanteSession) {
      setSession(adotanteSession);
      setAppLoadingMessage("");
      navigate("match");
      return;
    }
    const storedSession = loadSession();
    if (storedSession) {
      setSession(storedSession);
      setAppLoadingMessage("Entre com uma conta de adotante para fazer match.");
    } else {
      setSession(null);
    }
    navigate("login");
  };

  const goToSupport = () => {
    const adotanteSession = getAdotanteSession(session);
    if (adotanteSession) {
      setSession(adotanteSession);
    }
    setAppLoadingMessage("");
    navigate("support");
  };

  const openPetDetails = async (pet) => {
    setSelectedPet(pet);
    setActivePage("petDetails");
    setMenuOpen(false);
    window.scrollTo({ top: 0, behavior: "smooth" });
    if (!isApiPet(pet)) {
      return;
    }
    try {
      const animal = await buscarAnimal(pet.id);
      setSelectedPet(mapAnimal(animal));
      setAppLoadingMessage("");
    } catch (error) {
      if (isColdStartError(error)) {
        setAppLoadingMessage(COLD_START_MESSAGE);
      } else {
        setApiError(error.message);
      }
    }
  };

  const startAdoption = (pet) => {
    setSelectedPet(pet);
    navigate(session ? "adoption" : "login");
  };

  return (
    <div className="app-shell">
      <Header
        activePage={activePage}
        menuOpen={menuOpen}
        session={session}
        onLogout={logout}
        onMenu={() => setMenuOpen((open) => !open)}
        onNavigate={navigate}
        onMatch={goToMatch}
        onSupport={goToSupport}
      />

      <main>
        {appLoadingMessage && <div className="api-alert">{appLoadingMessage}</div>}
        {apiError && <div className="api-alert">Não foi possível sincronizar com a API: {apiError}</div>}
        {activePage === "home" && <HomeLanding pets={catalogPets} loading={catalogLoading} onNavigate={navigate} onMatch={goToMatch} onSelectPet={openPetDetails} />}
        {activePage === "pets" && <Encontrar pets={catalogPets} loading={catalogLoading} searchTerm={searchTerm} onSearch={setSearchTerm} onSelectPet={openPetDetails} />}
        {activePage === "petDetails" && <SobrePets pet={selectedPet} relatedPets={catalogPets.filter(isApiPet)} onBack={() => navigate("pets")} onNavigate={navigate} onAdopt={startAdoption} onMatch={goToMatch} onSelectPet={openPetDetails} />}
        {activePage === "how" && <Como onNavigate={navigate} />}
        {activePage === "orgs" && <Ongs />}
        {activePage === "stories" && <Historias onMatch={goToMatch} />}
        {activePage === "help" && <Ajuda onSupport={goToSupport} />}
        {activePage === "adoption" && <Form pet={selectedPet} session={session} onNavigate={navigate} onSubmitted={() => navigate("requests")} />}
        {activePage === "login" && <LoginCadastro onNavigate={navigate} onLoginSuccess={handleSession} />}
        {activePage === "match" && <Match session={session} onNavigate={navigate} onSelectPet={openPetDetails} />}
        {activePage === "requests" && <Solicitacoes session={session} onNavigate={navigate} />}
        {activePage === "support" && <Suporte session={session} onNavigate={navigate} />}
      </main>

      <Footer onNavigate={navigate} onSupport={goToSupport} />
    </div>
  );
}

function isApiPet(pet) {
  return pet?.id && String(pet.id).includes("-");
}

function Header({ activePage, menuOpen, session, onLogout, onMenu, onNavigate, onMatch, onSupport }) {
  const helpMenuOptions = [
    { label: "Central de ajuda", icon: HelpCircle, onClick: () => onNavigate("help") },
    ...helpOptions,
    { label: "Fale conosco", icon: MessageCircle, onClick: onSupport },
  ];

  return (
    <header className="site-header">
      <div className="header-row">
        <button
          className={menuOpen ? "mobile-menu active" : "mobile-menu"}
          onClick={onMenu}
          aria-label={menuOpen ? "Fechar menu" : "Abrir menu"}
          aria-expanded={menuOpen}
          aria-controls="main-navigation"
        >
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <button className="brand" onClick={() => onNavigate("home")} aria-label="MiAUmigos">
          <img src="/logo-miaumigos.svg" alt="MiAUmigos" />
        </button>

        <nav id="main-navigation" className={menuOpen ? "nav-links open" : "nav-links"} aria-label="Navegação principal">
          <Dropdown label="Encontrar Pets" active={activePage === "pets"} items={petCategories} onMain={() => onNavigate("pets")} />
          <NavButton active={activePage === "how"} onClick={() => onNavigate("how")}>
            Como funciona
          </NavButton>
          <NavButton active={activePage === "orgs"} onClick={() => onNavigate("orgs")}>
            ONGs & Protetores
          </NavButton>
          <NavButton active={activePage === "stories"} onClick={() => onNavigate("stories")}>
            Histórias
          </NavButton>
          <Dropdown label="Ajuda" active={activePage === "help" || activePage === "support"} items={helpMenuOptions} />
        </nav>

        <div className="header-actions">
          <button aria-label="Favoritos">
            <Heart size={18} />
          </button>
          <button aria-label="Solicitações" onClick={() => onNavigate(session ? "requests" : "login")}>
            <Bell size={18} />
          </button>
          <button aria-label="Perfil" onClick={() => onNavigate("login")}>
            <UserCircle size={19} />
          </button>
          {session && (
            <button aria-label="Sair" onClick={onLogout}>
              Sair
            </button>
          )}
          <button className="match-nav-button" onClick={onMatch}>
            <Heart size={16} fill="currentColor" />
            <span>Fazer Match</span>
          </button>
        </div>
      </div>
    </header>
  );
}

function NavButton({ active, children, onClick }) {
  return (
    <button className={active ? "nav-button active" : "nav-button"} onClick={onClick}>
      {children}
    </button>
  );
}

function Dropdown({ label, active, items, onMain }) {
  return (
    <div className="nav-dropdown">
      <button className={active ? "nav-button active" : "nav-button"} type="button" onClick={onMain}>
        {label}
        <ChevronDown size={14} />
      </button>
      <div className="dropdown-menu">
        {items.map(({ label: itemLabel, icon: Icon, onClick }) => (
          <button key={itemLabel} onClick={onClick || onMain}>
            <Icon size={16} />
            {itemLabel}
          </button>
        ))}
      </div>
    </div>
  );
}

function HomeLanding({ pets, loading, onNavigate, onMatch, onSelectPet }) {
  return (
    <>
      <Hero onMatch={onMatch} onNavigate={onNavigate} />
      <PetPreview pets={pets} loading={loading} onNavigate={onNavigate} onSelectPet={onSelectPet} />
      <HowItWorks />
      <StoriesBlock onNavigate={onNavigate} />
      <SupportBlock />
      <OrgsBlock onNavigate={onNavigate} />
    </>
  );
}

function Hero({ onMatch, onNavigate }) {
  return (
    <section className="hero-section">
      <div className="home-container hero-grid">
        <div className="hero-copy">
          <span className="hero-trust">
            <ShieldCheck size={16} />
            Adoção verificada e acompanhada
          </span>
          <h1>
            Encontre o seu <strong>melhor amigo.</strong>
          </h1>
          <p>
            Matching inteligente por estilo de vida, adoção segura e uma jornada mais clara para adotantes,
            ONGs e protetores.
          </p>

          <div className="hero-actions">
            <button className="primary-action" onClick={onMatch}>
              <Heart size={17} fill="currentColor" />
              Quero adotar
            </button>
            <button className="secondary-action hero-secondary-action" onClick={() => onNavigate("orgs")}>
              ONGs & Protetores
              <ChevronRight size={17} />
            </button>
          </div>
        </div>

        <div className="hero-visual" aria-label="Cachorro e gato para adoção">
          <div className="hero-floating-card hero-floating-card-top">
            <small>Match MiAUmigos</small>
            <strong>94% compatível</strong>
            <span>Rotina e perfil alinhados</span>
          </div>
          <img className="hero-pets-image" src="/2.svg" alt="Cachorro e gato disponíveis para adoção" />
          <div className="hero-floating-card hero-floating-card-bottom">
            <small>Rede de cuidado</small>
            <strong>ONGs verificadas</strong>
            <span>Contato seguro para adoção</span>
          </div>
        </div>
      </div>
    </section>
  );
}

function PetPreview({ pets, loading, onNavigate, onSelectPet }) {
  return (
    <section className="home-section">
      <div className="home-container">
        <div className="section-topline">
          <div>
            <span>Pets esperando por você</span>
            <h2>Perfis leves, claros e cheios de personalidade.</h2>
          </div>
          <button className="link-action" onClick={() => onNavigate("pets")}>
            Ver todos <ChevronRight size={16} />
          </button>
        </div>
        <div className="pet-grid pet-grid-home">
          {loading && <p>Carregando pets disponíveis...</p>}
          {!loading && pets.length === 0 && <p>Nenhum pet disponível encontrado.</p>}
          {!loading && pets.slice(0, 5).map((pet) => (
            <PetCard key={pet.id} pet={pet} onSelect={() => onSelectPet(pet)} />
          ))}
        </div>
      </div>
    </section>
  );
}

function HowItWorks() {
  const steps = [
    { title: "Cadastre-se", text: "Conte sobre você e o tipo de pet ideal.", icon: CalendarCheck, tone: "blue" },
    { title: "Receba matches", text: "Nosso algoritmo encontra pets compatíveis.", icon: PawPrint, tone: "yellow" },
    { title: "Converse", text: "Fale com a ONG ou protetor responsável.", icon: MessageCircle, tone: "green" },
    { title: "Adoção com amor", text: "Finalize a adoção e realize um novo melhor amigo.", icon: HomeIcon, tone: "pink" },
  ];

  return (
    <section className="home-section how-section">
      <div className="home-container how-layout">
        <div className="how-copy">
          <span>Como funciona?</span>
          <h2>Um processo simples, seguro e cheio de amor.</h2>
        </div>
        <div className="timeline">
          {steps.map(({ title, text, icon: Icon, tone }, index) => (
            <article className="timeline-step" key={title}>
              <div className={`timeline-icon timeline-icon-${tone}`}>
                <Icon size={28} />
              </div>
              <span>{index + 1}</span>
              <h3>{title}</h3>
              <p>{text}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

function StoriesBlock({ onNavigate }) {
  return (
    <section className="home-section">
      <div className="home-container">
        <div className="section-topline">
          <div>
            <span>Histórias que inspiram</span>
            <h2>Encontros reais, vidas transformadas.</h2>
          </div>
          <button className="link-action" onClick={() => onNavigate("stories")}>
            Ver histórias <ChevronRight size={16} />
          </button>
        </div>
        <div className="story-grid story-grid-home">
          {stories.map((story) => (
            <article className="story-card story-card-photo" key={story.title}>
              <img className="story-main-photo" src={story.tutorImage} alt={`Tutor de ${story.author}`} />
              <div>
                <small>História de amor</small>
                <h3>{story.title}</h3>
                <p>{story.text}</p>
                <strong>{story.author}</strong>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

function SupportBlock() {
  return (
    <section className="home-section">
      <div className="home-container support-block">
        <div className="support-icon">
          <Heart size={34} fill="currentColor" />
        </div>
        <div>
          <span>Rede de cuidado</span>
          <h2>Sua ajuda mantém pets cuidados e prontos para adoção.</h2>
        </div>
        <div className="support-actions">
          <button className="primary-action">
            <HandHeart size={17} />
            Seja voluntário
          </button>
          <button className="secondary-action">
            <Heart size={17} />
            Faça uma doação
          </button>
        </div>
      </div>
    </section>
  );
}

function OrgsBlock({ onNavigate }) {
  return (
    <section className="home-section orgs-section">
      <div className="home-container">
        <div className="section-topline">
          <div>
            <span>ONGs e protetores</span>
            <h2>Uma rede verificada de cuidado local.</h2>
          </div>
          <button className="link-action" onClick={() => onNavigate("orgs")}>
            Ver todas <ChevronRight size={16} />
          </button>
        </div>
        <div className="org-grid org-grid-home">
          {orgs.map((org, index) => (
            <article className="org-card org-card-home" key={org}>
              <div>
                {index % 3 === 0 && <PawPrint size={32} />}
                {index % 3 === 1 && <ShieldCheck size={32} />}
                {index % 3 === 2 && <Users size={32} />}
              </div>
              <h3>{org}</h3>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

function Footer({ onNavigate, onSupport }) {
  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-about">
          <button className="footer-brand" onClick={() => onNavigate("home")}>
            <img src="/logo-miaumigos-branco.svg" alt="MiAUmigos" />
          </button>
          <p>Conectando pets a lares cheios de amor em Pernambuco.</p>
          <div className="footer-social">
            <button aria-label="Instagram">
              <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18">
                <path
                  d="M7.6 2.75h8.8A4.86 4.86 0 0 1 21.25 7.6v8.8a4.86 4.86 0 0 1-4.85 4.85H7.6a4.86 4.86 0 0 1-4.85-4.85V7.6A4.86 4.86 0 0 1 7.6 2.75Zm0 1.7A3.15 3.15 0 0 0 4.45 7.6v8.8a3.15 3.15 0 0 0 3.15 3.15h8.8a3.15 3.15 0 0 0 3.15-3.15V7.6a3.15 3.15 0 0 0-3.15-3.15H7.6Zm4.4 3.18a4.37 4.37 0 1 1 0 8.74 4.37 4.37 0 0 1 0-8.74Zm0 1.7a2.67 2.67 0 1 0 0 5.34 2.67 2.67 0 0 0 0-5.34Zm4.58-2.36a1.02 1.02 0 1 1 0 2.04 1.02 1.02 0 0 1 0-2.04Z"
                  fill="currentColor"
                />
              </svg>
            </button>
            <button aria-label="Facebook">
              <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18">
                <path
                  d="M13.45 21.5v-8.3h2.8l.42-3.23h-3.22V7.91c0-.94.26-1.58 1.61-1.58h1.72V3.44a22.7 22.7 0 0 0-2.5-.13c-2.48 0-4.18 1.51-4.18 4.29v2.37H7.3v3.23h2.8v8.3h3.35Z"
                  fill="currentColor"
                />
              </svg>
            </button>
            <button aria-label="LinkedIn">
              <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18">
                <path
                  d="M6.94 8.98H3.7v10.6h3.24V8.98ZM5.32 4.2c-1.04 0-1.72.69-1.72 1.6 0 .89.66 1.59 1.68 1.59h.02c1.07 0 1.73-.7 1.73-1.59-.02-.91-.66-1.6-1.71-1.6Zm14.98 9.3c0-3.25-1.73-4.76-4.04-4.76-1.86 0-2.69 1.02-3.15 1.74v-1.5H9.87c.04.99 0 10.6 0 10.6h3.24v-5.92c0-.32.02-.63.12-.86.26-.63.84-1.29 1.82-1.29 1.29 0 1.8.98 1.8 2.41v5.66h3.24l.01-6.08Z"
                  fill="currentColor"
                />
              </svg>
            </button>
          </div>
        </div>

        <nav className="footer-column" aria-label="Navegar">
          <h3>Navegar</h3>
          <button onClick={() => onNavigate("pets")}>Encontrar Pets</button>
          <button onClick={() => onNavigate("pets")}>Cachorros</button>
          <button onClick={() => onNavigate("pets")}>Gatos</button>
          <button onClick={() => onNavigate("pets")}>Filhotes</button>
          <button onClick={() => onNavigate("stories")}>Histórias</button>
        </nav>

        <nav className="footer-column" aria-label="Institucional">
          <h3>Institucional</h3>
          <button>Sobre nós</button>
          <button onClick={() => onNavigate("orgs")}>ONGs & Protetores</button>
          <button onClick={() => onNavigate("how")}>Como funciona</button>
          <button>Termos de uso</button>
          <button>Privacidade</button>
        </nav>

        <nav className="footer-column" aria-label="Ajuda">
          <h3>Ajuda</h3>
          <button onClick={() => onNavigate("help")}>Central de ajuda</button>
          <button onClick={() => onNavigate("help")}>Dúvidas frequentes</button>
          <button>Regras da comunidade</button>
          <button onClick={onSupport}>Fale conosco</button>
        </nav>

        <form className="footer-newsletter">
          <label htmlFor="footer-email">Fique por dentro</label>
          <p>Receba histórias, novidades e dicas para cuidar do seu pet.</p>
          <div>
            <input id="footer-email" placeholder="Seu melhor e-mail" type="email" />
            <button aria-label="Enviar e-mail">
              <ChevronRight size={18} />
            </button>
          </div>
        </form>
      </div>
      <small>© 2026 MiAUmigos. Todos os direitos reservados.</small>
    </footer>
  );
}
