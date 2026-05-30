import { ArrowLeft, Heart, PawPrint } from "lucide-react";

export default function SobrePets({ pet, relatedPets = [], onBack, onAdopt, onMatch, onSelectPet }) {
  if (!pet) {
    return (
      <section className="pet-detail-page">
        <div className="pet-detail-empty">
          <h1>Pet não encontrado</h1>
          <button className="primary-action" onClick={onBack}>Voltar para pets</button>
        </div>
      </section>
    );
  }

  const otherAvailablePets = relatedPets
    .filter((relatedPet) => relatedPet.id !== pet.id)
    .slice(0, 4);

  return (
    <section className="pet-detail-page">
      <button className="detail-back" onClick={onBack}>
        <ArrowLeft size={17} />
        Voltar para pets
      </button>

      <div className="pet-detail-hero">
        <div className="pet-detail-photo">
          <img src={pet.image} alt={`${pet.name}, ${pet.type} para adoção`} />
          <span>{pet.status}</span>
        </div>

        <div className="pet-detail-copy">
          <span className="eyebrow">
            <PawPrint size={16} />
            Sobre este pet
          </span>
          <h1>{pet.name}</h1>
          <p>{pet.personality}</p>

          <div className="pet-detail-facts">
            <span>{pet.age}</span>
            <span>{pet.type}</span>
            <span>{pet.sex}</span>
            <span>{pet.size}</span>
            <span>{pet.status}</span>
          </div>

          <div className="pet-detail-tags">
            {pet.tags.map((tag) => (
              <small key={tag}>{tag}</small>
            ))}
          </div>

          <div className="pet-detail-actions">
            <button className="primary-action" onClick={() => onAdopt(pet)}>
              <Heart size={17} fill="currentColor" />
              Quero adotar
            </button>
            <button className="secondary-action" onClick={onMatch}>
              Ver compatibilidade
            </button>
          </div>
        </div>
      </div>

      {otherAvailablePets.length > 0 && (
        <section className="pet-detail-related">
          <div className="pet-detail-related-header">
            <span>
              <PawPrint size={16} />
              Disponíveis para adoção
            </span>
            <h2>Outros animais disponíveis</h2>
          </div>

          <div className="pet-detail-related-grid">
            {otherAvailablePets.map((relatedPet) => (
              <RelatedPetCard key={relatedPet.id} pet={relatedPet} onSelect={() => onSelectPet(relatedPet)} />
            ))}
          </div>
        </section>
      )}
    </section>
  );
}

function RelatedPetCard({ pet, onSelect }) {
  return (
    <article className="pet-detail-related-card">
      <button type="button" onClick={onSelect} aria-label={`Ver detalhes de ${pet.name}`}>
        <img src={pet.image} alt={`${pet.name}, ${pet.type} para adoção`} />
        <div>
          <span>{pet.status}</span>
          <h3>{pet.name}</h3>
          <p>
            {pet.type} • {pet.sex} • {pet.age} • {pet.size}
          </p>
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
