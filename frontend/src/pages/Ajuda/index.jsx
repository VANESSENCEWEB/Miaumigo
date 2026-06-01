import { ChevronDown, ChevronRight, HelpCircle, PawPrint } from "lucide-react";
import { SectionHeading } from "../Home/shared";
import { faqs } from "../Home/data";
import { useState } from "react";

export default function Ajuda({ onSupport }) {
  const [openFaq, setOpenFaq] = useState(0);

  return (
    <section className="faq-page">
      <div className="faq-copy">
        <SectionHeading
          eyebrow="FAQ"
          title="Perguntas frequentes"
          text="Tire suas dúvidas sobre adoção responsável e o processo para encontrar o seu melhor amigo."
        />
        <div className="faq-pets" aria-label="Cachorro e gato em destaque">
          <div className="faq-pets-blob" />
          <img src="/3.svg" alt="Cachorro e gato" />
        </div>
      </div>

      <div className="faq-list">
        {faqs.map((faq, index) => (
          <article className="faq-item" key={faq.question}>
            <button onClick={() => setOpenFaq(openFaq === index ? -1 : index)}>
              <span>
                <PawPrint size={22} />
              </span>
              <strong>{faq.question}</strong>
              <ChevronDown size={24} />
            </button>
            {openFaq === index && <p>{faq.answer}</p>}
          </article>
        ))}
      </div>

      <div className="contact-strip">
        <HelpCircle size={34} />
        <div>
          <h3>Ainda tem dúvidas?</h3>
          <p>Nossa equipe está pronta para te ajudar.</p>
        </div>
        <button className="secondary-action" onClick={onSupport}>
          Fale conosco
          <ChevronRight size={17} />
        </button>
      </div>
    </section>
  );
}
