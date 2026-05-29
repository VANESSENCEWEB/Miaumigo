import { Building2, ShieldCheck } from "lucide-react";
import { orgs } from "../Home/data";
import { SectionHeading } from "../Home/shared";

export default function Ongs({ homeBlock = false }) {
  return (
    <section className={homeBlock ? "content-section" : "content-section standalone-page"}>
      <SectionHeading
        eyebrow="ONGs que confiam no MiAUmigos"
        title="Uma rede de cuidado em Pernambuco"
        text="Parceiros e protetores independentes podem divulgar pets, acompanhar interessados e fortalecer adoções seguras."
      />
      <div className="org-grid">
        {orgs.map((org, index) => (
          <article className="org-card" key={org}>
            <Building2 size={26} />
            <h3>{org}</h3>
            <p>{index + 6} pets disponíveis para adoção responsável.</p>
            <span>
              <ShieldCheck size={15} />
              Perfil verificado
            </span>
          </article>
        ))}
      </div>
    </section>
  );
}
