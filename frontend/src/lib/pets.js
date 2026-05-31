const dogImages = [
	"/cachorro1.jpg",
	"/cachorro2.jpg",
	"/cachorrofemea1.png",
	"/cachorrofemea2.jpg",
	"/cachorromacho1.png",
	"/cachorromacho2.jpg",
	"/cachorromacho3.png",
];

const catImages = [
	"/gato1.jpg",
	"/gato2.jpeg",
	"/gato3.jpg",
	"/gato4.png",
	"/gato5.png",
	"/gatomacho1.png",
	"/gatomacho2.png",
];

const fallbackAnimalImages = [
	"/mock-animais/mock-pet-00.png",
	"/mock-animais/mock-pet-01.png",
	"/mock-animais/mock-pet-02.png",
	"/mock-animais/mock-pet-03.png",
	"/mock-animais/mock-pet-04.png",
	"/mock-animais/mock-pet-05.png",
	"/mock-animais/mock-pet-06.png",
	"/mock-animais/mock-pet-07.png",
	"/mock-animais/mock-pet-08.png",
];

const animalImagesByName = {
	BOB: "/cachorro1.jpg",
	LOLA: "/gatomacho1.png",
	LUNA: "/gato1.jpg",
	MEL: "/gato2.jpeg",
	MILO: "/gatomacho1.png",
	REX: "/cachorromacho1.png",
	THOR: "/cachorromacho2.jpg",
};

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

const sexoLabels = {
	MACHO: "Macho",
	FEMEA: "Fêmea",
	DESCONHECIDO: "Não informado",
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

export function mapAnimal(animal, image = localAnimalImage(animal)) {
	const tags = animal.tags || [];
	return {
		...animal,
		name: animal.nome,
		type: especieLabels[animal.especie] || animal.especie || "Pet",
		species: especieLabels[animal.especie] || animal.especie || "Pet",
		breed: especieLabels[animal.especie] || animal.especie || "SRD",
		size: porteLabels[animal.porte] || animal.porte || "Porte não informado",
		age: formatAge(animal.idade),
		sex: sexoLabels[animal.sexo] || "Não informado",
		image,
		badge: animal.compatibilidade !== undefined ? `${animal.compatibilidade}% match` : statusLabels[animal.status] || "Disponível",
		tags: tags.map((tag) => tagLabels[tag] || tag.toLowerCase()),
		status: statusLabels[animal.status] || animal.status || "Disponível",
		history: animal.descricao || "Este pet está aguardando uma família responsável.",
		personality: animal.descricao || "Conheça este pet e veja se ele combina com sua rotina.",
		score: animal.compatibilidade === undefined ? null : animal.compatibilidade,
	};
}

export function mapAnimals(animais) {
	const animaisUnicos = [];
	const chavesEncontradas = new Set();
	for (const animal of animais || []) {
		const chave = animalKey(animal);
		if (!chavesEncontradas.has(chave)) {
			chavesEncontradas.add(chave);
			animaisUnicos.push(animal);
		}
	}
	const imagensUsadas = {
		CACHORRO: new Set(),
		GATO: new Set(),
		OUTRO: new Set(),
	};
	return animaisUnicos.map((animal) => mapAnimal(animal, uniqueAnimalImage(animal, imagensUsadas)));
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

function animalKey(animal) {
	if (animal.cloudinary_public_id) {
		return `imagem:${String(animal.cloudinary_public_id).trim().toLowerCase()}`;
	}
	return [
		animal.nome,
		animal.especie,
		animal.porte,
		animal.idade,
		animal.descricao,
	].map((value) => String(value ?? "").trim().toLowerCase()).join("|");
}

function localAnimalImage(animal) {
	const hardcodedImage = hardcodedAnimalImage(animal);
	if (hardcodedImage) {
		return hardcodedImage;
	}
	const images = imagesFor(animal);
	return images[hashAnimal(animal) % images.length];
}

function uniqueAnimalImage(animal, imagensUsadas) {
	const hardcodedImage = hardcodedAnimalImage(animal);
	if (hardcodedImage) {
		return hardcodedImage;
	}
	const especie = animal.especie || "OUTRO";
	const images = imagesFor(animal);
	const used = imagensUsadas[especie] || imagensUsadas.OUTRO;
	const startIndex = hashAnimal(animal) % images.length;
	for (let offset = 0; offset < images.length; offset += 1) {
		const image = images[(startIndex + offset) % images.length];
		if (!used.has(image)) {
			used.add(image);
			return image;
		}
	}
	return images[startIndex];
}

function imagesFor(animal) {
	if (animal.especie === "CACHORRO") {
		return [...dogImages, ...fallbackAnimalImages];
	}
	if (animal.especie === "GATO") {
		return [...catImages, ...fallbackAnimalImages];
	}
	return fallbackAnimalImages;
}

function hardcodedAnimalImage(animal) {
	return animalImagesByName[normalizarNomeAnimal(animal.nome)] || null;
}

function normalizarNomeAnimal(nome) {
	return String(nome || "")
		.trim()
		.normalize("NFD")
		.replace(/[\u0300-\u036f]/g, "")
		.toUpperCase();
}

function hashAnimal(animal) {
	const value = [
		animal.id,
		animal.nome,
		animal.especie,
		animal.sexo,
		animal.porte,
		animal.idade,
	].map((item) => String(item ?? "")).join("|");
	let hash = 0;
	for (let index = 0; index < value.length; index += 1) {
		hash = (hash * 31 + value.charCodeAt(index)) >>> 0;
	}
	return hash;
}
