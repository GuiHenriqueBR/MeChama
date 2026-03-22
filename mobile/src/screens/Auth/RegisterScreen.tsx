import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { register, UserType } from '../../api/auth';

/**
 * Tela de cadastro do MeChama.
 *
 * Props:
 *  - navigation: objeto de navegação (React Navigation).
 *    Após cadastro bem-sucedido, navega para a tela principal
 *    (ajustar a rota conforme o stack definido no App.tsx).
 */
interface Props {
  navigation?: any;
}

export default function RegisterScreen({ navigation }: Props) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [type, setType] = useState<UserType>('CLIENT');
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    if (!name.trim() || !email.trim() || !password.trim()) {
      Alert.alert('Atenção', 'Preencha nome, e-mail e senha.');
      return;
    }

    setLoading(true);
    try {
      const user = await register(name.trim(), email.trim(), password, type, phone.trim() || undefined);
      Alert.alert('Cadastro realizado!', `Bem-vindo ao MeChama, ${user.name}!`, [
        {
          text: 'OK',
          onPress: () => {
            // TODO: navegar para Home após implementar a navegação principal
            // navigation?.replace('Home');
          },
        },
      ]);
    } catch (error: any) {
      const message =
        error?.response?.data?.message || 'Falha no cadastro. Tente novamente.';
      Alert.alert('Erro', message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>Criar conta</Text>
        <Text style={styles.subtitle}>MeChama — conecte-se a quem faz</Text>

        {/* Nome */}
        <Text style={styles.label}>Nome completo</Text>
        <TextInput
          style={styles.input}
          value={name}
          onChangeText={setName}
          placeholder="Seu nome"
          autoCapitalize="words"
        />

        {/* E-mail */}
        <Text style={styles.label}>E-mail</Text>
        <TextInput
          style={styles.input}
          value={email}
          onChangeText={setEmail}
          placeholder="seu@email.com"
          keyboardType="email-address"
          autoCapitalize="none"
          autoCorrect={false}
        />

        {/* Senha */}
        <Text style={styles.label}>Senha</Text>
        <TextInput
          style={styles.input}
          value={password}
          onChangeText={setPassword}
          placeholder="Mínimo 6 caracteres"
          secureTextEntry
        />

        {/* Telefone (opcional) */}
        <Text style={styles.label}>Telefone (opcional)</Text>
        <TextInput
          style={styles.input}
          value={phone}
          onChangeText={setPhone}
          placeholder="(11) 91234-5678"
          keyboardType="phone-pad"
        />

        {/* Tipo de conta */}
        <Text style={styles.label}>Tipo de conta</Text>
        <View style={styles.typeRow}>
          <TouchableOpacity
            style={[styles.typeBtn, type === 'CLIENT' && styles.typeBtnActive]}
            onPress={() => setType('CLIENT')}>
            <Text style={[styles.typeBtnText, type === 'CLIENT' && styles.typeBtnTextActive]}>
              Cliente
            </Text>
            <Text style={styles.typeBtnDesc}>Contratar serviços</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.typeBtn, type === 'PROVIDER' && styles.typeBtnActive]}
            onPress={() => setType('PROVIDER')}>
            <Text style={[styles.typeBtnText, type === 'PROVIDER' && styles.typeBtnTextActive]}>
              Prestador
            </Text>
            <Text style={styles.typeBtnDesc}>Oferecer serviços</Text>
          </TouchableOpacity>
        </View>

        {/* Botão de cadastro */}
        <TouchableOpacity
          style={[styles.button, loading && styles.buttonDisabled]}
          onPress={handleRegister}
          disabled={loading}>
          {loading ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <Text style={styles.buttonText}>Cadastrar</Text>
          )}
        </TouchableOpacity>

        {/* Link para login */}
        <TouchableOpacity onPress={() => navigation?.navigate('Login')}>
          <Text style={styles.linkText}>Já tem conta? Entrar</Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  scroll: { padding: 24, paddingBottom: 48 },
  title: { fontSize: 28, fontWeight: 'bold', color: '#111', marginBottom: 4 },
  subtitle: { fontSize: 14, color: '#666', marginBottom: 32 },
  label: { fontSize: 14, fontWeight: '600', color: '#333', marginBottom: 6, marginTop: 16 },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 12,
    fontSize: 15,
    backgroundColor: '#fafafa',
  },
  typeRow: { flexDirection: 'row', gap: 12, marginTop: 4 },
  typeBtn: {
    flex: 1,
    borderWidth: 1.5,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
  },
  typeBtnActive: { borderColor: '#000', backgroundColor: '#f0f0f0' },
  typeBtnText: { fontSize: 15, fontWeight: '600', color: '#666' },
  typeBtnTextActive: { color: '#000' },
  typeBtnDesc: { fontSize: 11, color: '#999', marginTop: 2 },
  button: {
    backgroundColor: '#000',
    borderRadius: 10,
    padding: 16,
    alignItems: 'center',
    marginTop: 32,
  },
  buttonDisabled: { opacity: 0.6 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  linkText: { textAlign: 'center', color: '#555', marginTop: 20, fontSize: 14 },
});
