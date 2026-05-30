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
import { buscarAnimal, clearSession, listarAnimaisDisponiveis, loadSession, saveSession } from "../../lib/api";
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
  const [session, setSession] = useState(() => loadSession());

  useEffect(() => {
    let active = true;

    async function loadCatalog() {
      setCatalogLoading(true);
      setApiError("");
      try {
        const animais = await listarAnimaisDisponiveis();
        if (active) {
          setCatalogPets(mapAnimals(animais));
        }
      } catch (error) {
        if (active) {
          setCatalogPets([]);
          setApiError(error.message);
        }
      } finally {
        if (active) {
          setCatalogLoading(false);
        }
      }
    }

    loadCatalog();
    return () => {
      active = false;
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
    navigate(session ? "match" : "login");
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
    } catch (error) {
      setApiError(error.message);
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
      />

      <main>
        {apiError && <div className="api-alert">Não foi possível sincronizar com a API: {apiError}</div>}
        {activePage === "home" && <HomeLanding pets={catalogPets} loading={catalogLoading} onNavigate={navigate} onMatch={goToMatch} onSelectPet={openPetDetails} />}
        {activePage === "pets" && <Encontrar pets={catalogPets} loading={catalogLoading} searchTerm={searchTerm} onSearch={setSearchTerm} onSelectPet={openPetDetails} />}
        {activePage === "petDetails" && <SobrePets pet={selectedPet} relatedPets={catalogPets.filter(isApiPet)} onBack={() => navigate("pets")} onNavigate={navigate} onAdopt={startAdoption} onMatch={goToMatch} onSelectPet={openPetDetails} />}
        {activePage === "how" && <Como onNavigate={navigate} />}
        {activePage === "orgs" && <Ongs />}
        {activePage === "stories" && <Historias />}
        {activePage === "help" && <Ajuda />}
        {activePage === "adoption" && <Form pet={selectedPet} session={session} onNavigate={navigate} onSubmitted={() => navigate("requests")} />}
        {activePage === "login" && <LoginCadastro onNavigate={navigate} onLoginSuccess={handleSession} />}
        {activePage === "match" && <Match session={session} onNavigate={navigate} onSelectPet={openPetDetails} />}
        {activePage === "requests" && <Solicitacoes session={session} onNavigate={navigate} />}
      </main>

      <Footer onNavigate={navigate} />
    </div>
  );
}

function isApiPet(pet) {
  return pet?.id && String(pet.id).includes("-");
}

function Header({ activePage, menuOpen, session, onLogout, onMenu, onNavigate, onMatch }) {
  return (
    <header className="site-header">
      <div className="header-row">
        <button className="mobile-menu" onClick={onMenu} aria-label="Abrir menu">
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <button className="brand" onClick={() => onNavigate("home")} aria-label="MiAUmigos">
          <img src="/logo-miaumigos.svg" alt="MiAUmigos" />
        </button>

        <nav className={menuOpen ? "nav-links open" : "nav-links"} aria-label="Navegação principal">
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
          <Dropdown label="Ajuda" active={activePage === "help"} items={helpOptions} onMain={() => onNavigate("help")} />
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
            Fazer Match
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
      <button className={active ? "nav-button active" : "nav-button"} onClick={onMain}>
        {label}
        <ChevronDown size={14} />
      </button>
      <div className="dropdown-menu">
        {items.map(({ label: itemLabel, icon: Icon }) => (
          <button key={itemLabel} onClick={onMain}>
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
      <Hero onMatch={onMatch} />
      <PetPreview pets={pets} loading={loading} onNavigate={onNavigate} onSelectPet={onSelectPet} />
      <HowItWorks />
      <StoriesBlock onNavigate={onNavigate} />
      <SupportBlock />
      <OrgsBlock onNavigate={onNavigate} />
    </>
  );
}

function Hero({ onMatch }) {
  return (
    <section className="hero-section">
      <div className="home-container hero-grid">
        <div className="hero-copy">
          <h1>
            Encontre seu <strong>novo melhor amigo.</strong>
          </h1>
          <p>Descubra pets compatíveis com sua rotina e transforme uma vida.</p>

          <div className="hero-actions">
            <button className="primary-action" onClick={onMatch}>
              <Heart size={17} fill="currentColor" />
              Encontrar meu match
            </button>
          </div>

          <div className="trust-row">
            {trustBadges.map((badge) => (
              <span key={badge}>
                <CheckCircle2 size={16} />
                {badge}
              </span>
            ))}
          </div>
        </div>

        <div className="hero-visual" aria-label="Cachorro e gato para adoção">
          <img className="hero-pets-image" src="/2.svg" alt="Cachorro e gato disponíveis para adoção" />
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
    { title: "Cadastre-se", text: "Conte sobre você e o tipo de pet ideal.", icon: CalendarCheck },
    { title: "Receba matches", text: "Nosso algoritmo encontra pets compatíveis.", icon: PawPrint },
    { title: "Converse", text: "Fale com a ONG ou protetor responsável.", icon: MessageCircle },
    { title: "Adoção com amor", text: "Finalize a adoção e realize um novo melhor amigo.", icon: HomeIcon },
  ];

  return (
    <section className="home-section how-section">
      <div className="home-container how-layout">
        <div className="how-copy">
          <span>Como funciona?</span>
          <h2>Um processo simples, seguro e cheio de amor.</h2>
        </div>
        <div className="timeline">
          {steps.map(({ title, text, icon: Icon }, index) => (
            <article className="timeline-step" key={title}>
              <div className="timeline-icon">
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

function Footer({ onNavigate }) {
  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-about">
          <button className="footer-brand" onClick={() => onNavigate("home")}>
            <img src="/logo-miaumigos-branco.svg" alt="MiAUmigos" />
          </button>
          <p>Conectando pets a lares cheios de amor em Pernambuco.</p>
          <div className="footer-social">
            <button aria-label="Instagram">ig</button>
            <button aria-label="Facebook">f</button>
            <button aria-label="Comunidade">◎</button>
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
          <button>Fale conosco</button>
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
