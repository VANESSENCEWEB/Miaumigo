import { ArrowLeft, CheckCircle2, Heart, MapPin, PawPrint, ShieldCheck } from "lucide-react";

export default function SobrePets({ pet, onBack, onAdopt, onMatch }) {
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
            <span>{pet.sex}</span>
            <span>{pet.size}</span>
            <span>{pet.breed}</span>
          </div>

          <div className="pet-detail-location">
            <MapPin size={17} />
            {pet.city}, PE • {pet.distance} de você
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

      <div className="pet-detail-grid">
        <article>
          <ShieldCheck size={24} />
          <h2>Histórico de saúde</h2>
          <ul>
            {(pet.health || []).map((item) => (
              <li key={item}>
                <CheckCircle2 size={16} />
                {item}
              </li>
            ))}
          </ul>
        </article>

        <article>
          <PawPrint size={24} />
          <h2>História</h2>
          <p>{pet.history}</p>
        </article>

        <article>
          <Heart size={24} />
          <h2>Perfil ideal</h2>
          <p>
            Família disposta a seguir adaptação gradual, manter cuidados veterinários e oferecer rotina segura.
          </p>
        </article>
      </div>
    </section>
  );
}
