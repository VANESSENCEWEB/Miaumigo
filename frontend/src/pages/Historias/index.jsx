import { Heart } from "lucide-react";
import { stories } from "../Home/data";
import { SectionHeading } from "../Home/shared";

export default function Historias({ homeBlock = false }) {
  return (
    <section className={homeBlock ? "content-section soft-band" : "content-section standalone-page"}>
      <SectionHeading
        eyebrow="Adoções que transformaram vidas"
        title="Relatos de quem encontrou um novo amigo"
        text="Histórias reais com tutores e pets que ganharam uma nova rotina juntos."
      />
      <div className="story-grid story-grid-home">
        {stories.map((story) => (
          <article className="story-card story-card-photo" key={story.title}>
            <img className="story-main-photo" src={story.tutorImage} alt={`Tutor em ${story.title}`} />
            <div>
              <small>
                <Heart size={13} fill="currentColor" />
                História de amor
              </small>
              <h3>{story.title}</h3>
              <p>{story.text}</p>
              <strong>{story.author}</strong>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
