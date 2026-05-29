import { Heart, MapPin } from "lucide-react";

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
  return (
    <article className="pet-card">
      <button className="pet-card-open" onClick={onSelect} type="button" aria-label={`Ver detalhes de ${pet.name}`}>
        <div className="pet-image-wrap">
          <img src={pet.image} alt={`${pet.name}, ${pet.type} para adoção`} />
          <small>{pet.badge}</small>
          <span className="pet-favorite" aria-label={`Favoritar ${pet.name}`}>
            <Heart size={19} />
          </span>
        </div>
        <div className="pet-card-body">
          <div className="pet-card-title">
            <h3>{pet.name}</h3>
            <span>{pet.type}</span>
          </div>
          <p>
            {pet.age} • {pet.size}
          </p>
          <span className="pet-location">
            <MapPin size={14} />
            {pet.distance || pet.city} de você
          </span>
          <div className="tags">
            {pet.tags.slice(0, 2).map((tag) => (
              <small key={tag}>{tag}</small>
            ))}
          </div>
        </div>
      </button>
    </article>
  );
}
