# MiAumigos 🐾

Plataforma de adoção responsável de pets. A ideia é conectar quem quer adotar com animais disponíveis, tornando o processo mais simples e humano — sem burocracia desnecessária.

O projeto ainda está em desenvolvimento. Por enquanto cobre o fluxo de cadastro e listagem de pets, com planos de expandir para perfis de adotantes e acompanhamento pós-adoção.

---

## Rodando o projeto

```bash
npm install
npm run dev
```

Abre em `http://localhost:5173`.

### Imagens dos animais

O frontend prioriza o campo `cloudinary_public_id` retornado pela API para renderizar a imagem de cada animal. Esse campo pode ser:

- uma URL completa, como `https://...`;
- um caminho público local, como `/gato2.jpeg`;
- um `public_id` do Cloudinary, como `animais/luna`.

Para usar `public_id` do Cloudinary, configure uma destas variáveis:

```bash
VITE_CLOUDINARY_CLOUD_NAME=seu_cloud_name
# ou
VITE_CLOUDINARY_DELIVERY_BASE_URL=https://res.cloudinary.com/seu_cloud_name/image/upload
```

Se o animal não tiver imagem cadastrada ou o Cloudinary não estiver configurado, o app usa as imagens locais de fallback.

---

## Scripts

```bash
npm run dev       # desenvolvimento
npm run build     # build de produção
npm run preview   # visualizar o build
npm run lint      # checar erros de lint
```

---

## Stack

- **React 19 + Vite 8** — base do projeto
- **TailwindCSS** — estilização
- **TanStack Router** — roteamento
- **Radix UI** — componentes acessíveis (Dialog, Select, Switch...)
- **Lucide React** — ícones
- **Poppins** — fonte principal

---

## Estrutura

```
src/
├── components/    # componentes reutilizáveis
├── pages/         # páginas da aplicação
├── routes/        # configuração de rotas
└── main.jsx       # entrada da aplicação
```

---

## Deploy

```bash
npm run build
```

Pasta `dist/` pronta pra subir na Vercel, Netlify, ou qualquer servidor estático.
