import { Heart } from "lucide-react";

export function SectionHeading({ eyebrow, title, text }) {
  return (
    <div className="section-heading">
      <span>{eyebrow}</span>
      <h2>{title}</h2>
      {text && <p>{text}</p>}
    </div>
  );
}

export function PetCard({ pet, onSelect }) {
  const hasScore = pet.score !== null && pet.score !== undefined;

  return (
    <article className="pet-card">
      <button className="pet-card-open" onClick={onSelect} type="button" aria-label={`Ver detalhes de ${pet.name}`}>
        <div className="pet-image-wrap">
          <img src={pet.image} alt={`${pet.name}, ${pet.type} para adoção`} />
          <small className={hasScore ? "pet-badge pet-badge-match" : "pet-badge"}>{pet.badge}</small>
          <span className="pet-favorite" aria-label={`Favoritar ${pet.name}`}>
            <Heart size={19} />
          </span>
        </div>
        <div className="pet-card-body">
          <div className="pet-card-title">
            <h3>{pet.name}</h3>
            <span>{pet.type}</span>
          </div>
          <p className="pet-card-meta">
            {pet.age} • {pet.size} • {pet.sex}
          </p>
          <div className="tags">
            {pet.tags.slice(0, 2).map((tag) => (
              <small key={tag}>{tag}</small>
            ))}
          </div>
          <span className="pet-card-cta">Ver perfil completo</span>
        </div>
      </button>
    </article>
  );
}
