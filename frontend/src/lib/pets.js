const fallbackImages = [
	"/gato2.jpeg",
	"/cachorrofemea1.png",
	"/cachorro1.jpg",
	"/cachorromacho1.png",
	"/gatomacho1.png",
	"/cachorro2.jpg",
	"/cachorromacho2.jpg",
	"/gato1.jpg",
	"/gato3.jpg",
	"/gatomacho2.png",
	"/cachorrofemea2.jpg",
	"/cachorromacho3.png",
];

const especieLabels = {
	CACHORRO: "Cachorro",
	GATO: "Gato",
	OUTRO: "Outro",
};

const porteLabels = {
	PEQUENO: "Pequeno",
	MEDIO: "Médio",
	GRANDE: "Grande",
};

const statusLabels = {
	DISPONIVEL: "Disponível",
	EM_PROCESSO: "Em processo",
	ADOTADO: "Adotado",
};

const tagLabels = {
	DOCIL: "dócil",
	BRINCALHAO: "brincalhão",
	CALMO: "calmo",
	INDEPENDENTE: "independente",
	CARINHOSO: "carinhoso",
	SOCIAL: "sociável",
	PROTETOR: "protetor",
	ENERGICO: "energético",
	ADAPTADO_A_APARTAMENTO: "ideal para apartamento",
	PRECISA_DE_ESPACO: "precisa de espaço",
	CONVIVE_COM_CRIANCAS: "convive com crianças",
	CONVIVE_COM_CAES: "convive com cães",
	CONVIVE_COM_GATOS: "convive com gatos",
};

export const tagOptions = Object.entries(tagLabels).map(([value, label]) => ({ value, label }));

export function mapAnimal(animal, index = 0) {
	const tags = animal.tags || [];
	return {
		...animal,
		name: animal.nome,
		type: especieLabels[animal.especie] || animal.especie || "Pet",
		species: especieLabels[animal.especie] || animal.especie || "Pet",
		breed: especieLabels[animal.especie] || animal.especie || "SRD",
		size: porteLabels[animal.porte] || animal.porte || "Porte não informado",
		age: formatAge(animal.idade),
		sex: "Não informado",
		city: "Pernambuco",
		distance: "Disponível",
		image: imageFrom(animal.cloudinary_public_id, index),
		badge: animal.compatibilidade !== undefined ? `${animal.compatibilidade} match` : statusLabels[animal.status] || "Disponível",
		tags: tags.map((tag) => tagLabels[tag] || tag.toLowerCase()),
		status: statusLabels[animal.status] || animal.status || "Disponível",
		health: ["Informações de saúde serão confirmadas pelo lar responsável."],
		history: animal.descricao || "Este pet está aguardando uma família responsável.",
		personality: animal.descricao || "Conheça este pet e veja se ele combina com sua rotina.",
		score: animal.compatibilidade === undefined ? null : animal.compatibilidade,
	};
}

export function mapAnimals(animais) {
	return (animais || []).map((animal, index) => mapAnimal(animal, index));
}

function formatAge(age) {
	if (age === null || age === undefined) {
		return "Idade não informada";
	}
	if (age === 1) {
		return "1 ano";
	}
	return `${age} anos`;
}

function imageFrom(publicId, index) {
	if (publicId?.startsWith("http://") || publicId?.startsWith("https://") || publicId?.startsWith("/")) {
		return publicId;
	}
	return fallbackImages[index % fallbackImages.length];
}
