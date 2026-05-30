import {
  Cat,
  Dog,
  HandHeart,
  Heart,
  Home as HomeIcon,
  PawPrint,
  Users,
} from "lucide-react";

export const petCategories = [
  { label: "Cachorros", icon: Dog },
  { label: "Gatos", icon: Cat },
  { label: "Filhotes", icon: PawPrint },
  { label: "Pequeno porte", icon: Heart },
];

export const helpOptions = [
  { label: "Ser voluntário", icon: HandHeart },
  { label: "Fazer doação", icon: Heart },
  { label: "Lar temporário", icon: HomeIcon },
  { label: "Parceiros", icon: Users },
];

export const orgs = [
  "Amor de Patas PE",
  "Abrigo Miados",
  "Protetores Recife",
  "Lar Temporário Azul",
  "Coração Animal",
  "Patinhas do Bem",
];

export const stories = [
  {
    title: "O Thor mudou nossa vida",
    text: "Ele chegou tímido e hoje espera todo mundo na porta.",
    author: "Juliana & Thor",
    tutorImage: "/pessoa1.png",
  },
  {
    title: "A Nina era o que faltava",
    text: "A casa ficou mais leve desde o primeiro ronronar.",
    author: "Raphael & Nina",
    tutorImage: "/pessoa3.png",
  },
  {
    title: "Bento trouxe alegria",
    text: "Cada passeio virou um pequeno ritual em família.",
    author: "Fernanda & Bento",
    tutorImage: "/pessoa2.png",
  },
];

export const faqs = [
  {
    question: "Como funciona o processo de adoção?",
    answer: "O MIAUmigos conecta adotantes e pets com base em compatibilidade de perfil, rotina e necessidades do animal. Após o match, o responsável pelo pet pode continuar o processo de avaliação e aprovação da adoção.",
  },
  {
    question: "O que é o Match Inteligente?",
    answer: "É o sistema que analisa informações do adotante e do pet para identificar maior compatibilidade entre ambos, ajudando a promover adoções mais responsáveis e duradouras.",
  },
  {
    question: "Como o MIAUmigos apoia ONGs e protetores?",
    answer: "A plataforma busca conectar adotantes, ONGs e protetores de forma segura e acessível, além de incentivar visibilidade, apoio e futuras iniciativas de doação para ajudar no cuidado dos animais.",
  },
  {
    question: "Quais documentos são necessários para efetuar uma doação?",
    answer: "Normalmente nome, CPF, contato e comprovante do pagamento quando a doação for financeira.",
  },
    {
    question: "Como funciona a verificação do adotante?",
    answer: "O sistema realiza uma validação básica de identidade, endereço e informações do ambiente para trazer mais segurança ao processo de adoção.",
  },
      {
    question: "Quais informações são analisadas no formulário?",
    answer: "São analisadas informações como rotina, espaço disponível, experiência com animais, presença de outros pets e estilo de vida do adotante.",
  },
    {
    question: "Posso cadastrar um pet para adoção?",
    answer: "Sim. ONGs e protetores independentes podem cadastrar animais diretamente na plataforma.",
  },
      {
    question: "O que acontece após a adoção?",
    answer: "Após a adoção, o sistema realiza check-ins automatizados durante os primeiros 90 dias para acompanhar a adaptação do pet e do adotante.",
  },
      {
    question: "O que são os check-ins de 90 dias?",
    answer: "São acompanhamentos automáticos feitos pela plataforma para ajudar no processo de adaptação e incentivar uma adoção responsável.",
  },
];
