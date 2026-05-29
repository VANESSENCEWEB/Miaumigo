import { useMemo, useState } from "react";
import {
  CalendarDays,
  Cat,
  ChevronDown,
  Dog,
  Heart,
  PawPrint,
  SlidersHorizontal,
} from "lucide-react";

export default function Encontrar({ pets, loading, searchTerm, onSearch, onSelectPet }) {
  const [filters, setFilters] = useState({
    type: "Todos",
    size: "Todos",
    age: "Todas",
    tag: "Todas",
  });

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredPets = useMemo(() => {
    return pets.filter((pet) => {
      const matchesSearch =
        !normalizedSearch ||
        [pet.name, pet.type, pet.species, pet.breed, pet.size, pet.age, ...pet.tags]
          .join(" ")
          .toLowerCase()
          .includes(normalizedSearch);

      const matchesFilters =
        (filters.type === "Todos" || pet.type === filters.type) &&
        (filters.size === "Todos" || pet.size === filters.size) &&
        (filters.age === "Todas" || pet.age === filters.age) &&
        (filters.tag === "Todas" || pet.tags.includes(filters.tag));

      return matchesSearch && matchesFilters;
    });
  }, [filters, normalizedSearch, pets]);

  const updateFilter = (key, value) => setFilters((current) => ({ ...current, [key]: value }));

  return (
    <section className="find-page">
      <div className="find-hero">
        <div>
          <h1>
            Encontre o seu novo <strong>melhor amigo</strong>
          </h1>
          <p>Conheça os pets disponíveis para adoção e transforme uma vida.</p>
        </div>
      </div>

      <div className="filters-panel">
        <FilterSelect icon={PawPrint} label="Espécie" value={filters.type} onChange={(value) => updateFilter("type", value)} options={["Todos", "Cachorro", "Gato", "Outro"]} />
        <FilterSelect icon={Cat} label="Perfil" value={filters.tag} onChange={(value) => updateFilter("tag", value)} options={["Todas", "calmo", "carinhoso", "brincalhão", "sociável", "ideal para apartamento", "convive com crianças"]} />
        <FilterSelect icon={Dog} label="Porte" value={filters.size} onChange={(value) => updateFilter("size", value)} options={["Todos", "Pequeno", "Médio", "Grande"]} />
        <FilterSelect icon={CalendarDays} label="Idade" value={filters.age} onChange={(value) => updateFilter("age", value)} options={["Todas", "Idade não informada", "1 ano", "2 anos", "3 anos", "4 anos", "5 anos"]} />
        <FilterSelect icon={SlidersHorizontal} label="Mais filtros" value="Todos" onChange={() => {}} options={["Todos", "Disponível"]} />
      </div>

      <div className="results-row find-results-row">
        <p>
          <strong>{filteredPets.length}</strong> pets encontrados
        </p>
        <label>
          Ordenar por:
          <select defaultValue="recentes">
            <option value="recentes">Mais recentes</option>
            <option value="nome">Nome</option>
            <option value="cidade">Cidade</option>
          </select>
        </label>
      </div>

      {normalizedSearch && (
        <button className="clear-search" onClick={() => onSearch("")}>
          Limpar busca por "{searchTerm}"
        </button>
      )}

      <div className="find-grid">
        {loading && <p>Carregando pets disponíveis...</p>}
        {!loading && filteredPets.length === 0 && <p>Nenhum pet disponível encontrado.</p>}
        {filteredPets.map((pet) => (
          <FindPetCard key={pet.id} pet={pet} onSelect={() => onSelectPet(pet)} />
        ))}
      </div>
    </section>
  );
}

function FilterSelect({ icon: Icon, label, value, options, onChange }) {
  return (
    <label className="filter-select">
      <Icon size={16} />
      <span>{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)}>
        {options.map((option) => (
          <option key={option}>{option}</option>
        ))}
      </select>
      <ChevronDown size={14} />
    </label>
  );
}

function FindPetCard({ pet, onSelect }) {
  return (
    <article className="find-pet-card">
      <button className="find-pet-open" onClick={onSelect} aria-label={`Ver detalhes de ${pet.name}`}>
        <span>{pet.badge}</span>
        <img src={pet.image} alt={`${pet.name}, ${pet.type} para adoção`} />
        <Heart className="find-favorite" size={17} />
        <div>
          <h3>{pet.name}</h3>
          <p>
            {pet.age} • {pet.size}
          </p>
          <small>{pet.status}</small>
        </div>
      </button>
    </article>
  );
}
