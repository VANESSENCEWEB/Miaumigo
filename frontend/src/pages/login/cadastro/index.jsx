import {
  ArrowRight,
  CheckCircle2,
  Eye,
  Heart,
  HeartHandshake,
  Lock,
  Mail,
  MapPin,
  PawPrint,
  ShieldCheck,
  User,
  UserPlus,
} from "lucide-react";
import { useState } from "react";
import { cadastrarAdotante, login } from "../../../lib/api";
import { tagOptions } from "../../../lib/pets";

export default function LoginCadastro({ onNavigate, onLoginSuccess }) {
  const [mode, setMode] = useState("login");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [form, setForm] = useState({
    nome: "",
    endereco: "",
    email: "",
    senha: "",
    confirmarSenha: "",
    cpf: "",
    preferencias: ["CARINHOSO", "CALMO"],
  });
  const isLogin = mode === "login";

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const togglePreference = (value) => {
    setForm((current) => {
      const selected = current.preferencias.includes(value)
        ? current.preferencias.filter((item) => item !== value)
        : [...current.preferencias, value];
      return { ...current, preferencias: selected };
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      if (!isLogin) {
        if (form.senha !== form.confirmarSenha) {
          throw new Error("As senhas não conferem.");
        }
        await cadastrarAdotante({
          nome: form.nome,
          endereco: form.endereco,
          email: form.email,
          senha: form.senha,
          cpf: form.cpf,
          preferencias: form.preferencias,
        });
      }
      const session = await login({ email: form.email, senha: form.senha });
      onLoginSuccess(session);
      onNavigate("match");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-page standalone-page">
      <div className="auth-panel">
        <div className="auth-copy">
          <span>
            <Heart size={14} fill="currentColor" />
            MiAUmigos Match
          </span>
          <h1>{isLogin ? "Bem-vindo de volta!" : "Crie sua conta"}</h1>
          <p>
            {isLogin
              ? "Entre para continuar com segurança e ver pets compatíveis com sua rotina."
              : "Crie sua conta e descubra pets que combinam com seu lar e seu coração."}
          </p>

          <div className="auth-benefits">
            <span>
              <CheckCircle2 size={18} />
              Compatibilidade baseada na sua rotina
            </span>
            <span>
              <CheckCircle2 size={18} />
              Pets ideais para seu estilo de vida
            </span>
            <span>
              <CheckCircle2 size={18} />
              Contato direto com ONGs e protetores
            </span>
          </div>

          <div className="auth-pet-visual">
            <img src="/gatoecachorro-imagem.png" alt="Cachorro feliz para adoção" />
            <div>
              <Heart size={18} fill="currentColor" />
              <strong>Encontre um amigo. </strong>
              <small>Transforme uma vida.</small>
            </div>
          </div>
        </div>

        <form className="auth-card" onSubmit={submit}>
          <div className="auth-tabs" aria-label="Alternar login e cadastro">
            <button className={isLogin ? "active" : ""} type="button" onClick={() => setMode("login")}>
              <Mail size={16} />
              Login
            </button>
            <button className={!isLogin ? "active" : ""} type="button" onClick={() => setMode("cadastro")}>
              <UserPlus size={16} />
              Criar conta
            </button>
          </div>

          {!isLogin && (
            <label>
              Nome completo
              <span className="auth-field">
                <User size={16} />
                <input value={form.nome} onChange={(event) => updateField("nome", event.target.value)} placeholder="Seu nome completo" required={!isLogin} />
              </span>
            </label>
          )}

          <label>
            E-mail
            <span className="auth-field">
              <Mail size={16} />
              <input value={form.email} onChange={(event) => updateField("email", event.target.value)} type="email" placeholder="exemplo@email.com" required />
            </span>
          </label>

          <label>
            Senha
            <span className="auth-field">
              <Lock size={16} />
              <input value={form.senha} onChange={(event) => updateField("senha", event.target.value)} type={showPassword ? "text" : "password"} placeholder="********" required />
              <button
                className="auth-eye-button"
                type="button"
                onClick={() => setShowPassword((current) => !current)}
                aria-label={showPassword ? "Ocultar senha" : "Mostrar senha"}
              >
                <Eye size={16} />
              </button>
            </span>
          </label>

          {!isLogin && (
            <>
              <label>
                Confirme sua senha
                <span className="auth-field">
                  <Lock size={16} />
                  <input value={form.confirmarSenha} onChange={(event) => updateField("confirmarSenha", event.target.value)} type={showConfirmPassword ? "text" : "password"} placeholder="********" required={!isLogin} />
                  <button
                    className="auth-eye-button"
                    type="button"
                    onClick={() => setShowConfirmPassword((current) => !current)}
                    aria-label={showConfirmPassword ? "Ocultar confirmação de senha" : "Mostrar confirmação de senha"}
                  >
                    <Eye size={16} />
                  </button>
                </span>
              </label>

              <label>
                Endereço
                <span className="auth-field">
                  <MapPin size={16} />
                  <input value={form.endereco} onChange={(event) => updateField("endereco", event.target.value)} placeholder="Rua, número, cidade" required={!isLogin} />
                </span>
              </label>

              <label>
                CPF
                <span className="auth-field">
                  <ShieldCheck size={16} />
                  <input value={form.cpf} onChange={(event) => updateField("cpf", event.target.value)} placeholder="Somente números" required={!isLogin} />
                </span>
              </label>

              <fieldset className="auth-preferences">
                <legend>Preferências</legend>
                <div>
                  {tagOptions.map((option) => (
                    <label key={option.value}>
                      <input
                        type="checkbox"
                        checked={form.preferencias.includes(option.value)}
                        onChange={() => togglePreference(option.value)}
                      />
                      {option.label}
                    </label>
                  ))}
                </div>
              </fieldset>
            </>
          )}

          {isLogin && (
            <div className="auth-options">
              <label>
                <input type="checkbox" defaultChecked />
                Lembrar de mim
              </label>
              <button type="button">Esqueci minha senha</button>
            </div>
          )}

          {message && <p className="form-message">{message}</p>}

          <button className="primary-action auth-submit" type="submit" disabled={loading}>
            <PawPrint size={17} />
            {loading ? "Aguarde..." : isLogin ? "Encontrar meu match" : "Criar conta e encontrar meu match"}
            <ArrowRight size={17} />
          </button>

          <button className="auth-demo-link" type="button" onClick={() => onNavigate("match")}>
            <HeartHandshake size={17} />
            Experimentar demonstração
          </button>

          <p className="auth-safe-note">
            <ShieldCheck size={15} />
            Seus dados estão protegidos e seguros com a gente.
          </p>
        </form>
      </div>
    </section>
  );
}
