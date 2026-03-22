import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
  ActivityIndicator,
  Switch,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { getMyProfile, upsertMyProfile, ProviderProfileRequest } from '../../api/providers';

/**
 * Tela de edição do perfil do prestador (autenticado).
 * Acesso exclusivo para ROLE_PROVIDER.
 *
 * Especialidades são gerenciadas como tags separadas por vírgula
 * para simplificar o MVP. Refatorar para chips/autocomplete na v2.
 */
interface Props {
  navigation?: any;
}

export default function EditProfileScreen({ navigation }: Props) {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [bio, setBio] = useState('');
  const [city, setCity] = useState('');
  const [neighborhood, setNeighborhood] = useState('');
  const [experienceYears, setExperienceYears] = useState('');
  const [specialtiesText, setSpecialtiesText] = useState('');
  const [serviceRadiusKm, setServiceRadiusKm] = useState('20');
  const [available, setAvailable] = useState(true);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const profile = await getMyProfile();
      setBio(profile.bio ?? '');
      setCity(profile.city ?? '');
      setNeighborhood(profile.neighborhood ?? '');
      setExperienceYears(profile.experienceYears?.toString() ?? '');
      setSpecialtiesText(profile.specialties?.join(', ') ?? '');
      setServiceRadiusKm(profile.serviceRadiusKm?.toString() ?? '20');
      setAvailable(profile.available);
    } catch {
      Alert.alert('Erro', 'Não foi possível carregar seu perfil.');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const specialties = specialtiesText
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);

      const request: ProviderProfileRequest = {
        bio: bio || undefined,
        city: city || undefined,
        neighborhood: neighborhood || undefined,
        experienceYears: experienceYears ? parseInt(experienceYears, 10) : undefined,
        specialties: specialties.length > 0 ? specialties : undefined,
        serviceRadiusKm: serviceRadiusKm ? parseInt(serviceRadiusKm, 10) : undefined,
        available,
      };

      await upsertMyProfile(request);
      Alert.alert('Sucesso', 'Perfil atualizado com sucesso!', [
        { text: 'OK', onPress: () => navigation?.goBack() },
      ]);
    } catch (error: any) {
      const message =
        error?.response?.data?.message || 'Falha ao salvar o perfil.';
      Alert.alert('Erro', message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#000" />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>Editar Perfil</Text>

        <Text style={styles.label}>Bio / Apresentação</Text>
        <TextInput
          style={[styles.input, styles.inputMultiline]}
          value={bio}
          onChangeText={setBio}
          placeholder="Conte um pouco sobre você e sua experiência..."
          multiline
          numberOfLines={4}
          maxLength={1000}
        />

        <Text style={styles.label}>Cidade</Text>
        <TextInput
          style={styles.input}
          value={city}
          onChangeText={setCity}
          placeholder="Ex.: São Paulo"
        />

        <Text style={styles.label}>Bairro</Text>
        <TextInput
          style={styles.input}
          value={neighborhood}
          onChangeText={setNeighborhood}
          placeholder="Ex.: Vila Madalena"
        />

        <Text style={styles.label}>Anos de Experiência</Text>
        <TextInput
          style={styles.input}
          value={experienceYears}
          onChangeText={setExperienceYears}
          placeholder="Ex.: 5"
          keyboardType="numeric"
        />

        <Text style={styles.label}>Especialidades (separadas por vírgula)</Text>
        <TextInput
          style={styles.input}
          value={specialtiesText}
          onChangeText={setSpecialtiesText}
          placeholder="Ex.: Elétrica residencial, Instalação de ar-condicionado"
        />

        <Text style={styles.label}>Raio de Atendimento (km)</Text>
        <TextInput
          style={styles.input}
          value={serviceRadiusKm}
          onChangeText={setServiceRadiusKm}
          keyboardType="numeric"
          placeholder="Ex.: 20"
        />

        <View style={styles.switchRow}>
          <View>
            <Text style={styles.label}>Disponível para novos pedidos</Text>
            <Text style={styles.switchDesc}>
              {available ? 'Você aparece nos resultados de busca' : 'Você está oculto na busca'}
            </Text>
          </View>
          <Switch
            value={available}
            onValueChange={setAvailable}
            trackColor={{ true: '#000', false: '#ccc' }}
            thumbColor="#fff"
          />
        </View>

        <TouchableOpacity
          style={[styles.button, saving && styles.buttonDisabled]}
          onPress={handleSave}
          disabled={saving}>
          {saving ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <Text style={styles.buttonText}>Salvar alterações</Text>
          )}
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  scroll: { padding: 24, paddingBottom: 48 },
  title: { fontSize: 24, fontWeight: 'bold', color: '#111', marginBottom: 24 },
  label: { fontSize: 14, fontWeight: '600', color: '#333', marginBottom: 6, marginTop: 16 },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 12,
    fontSize: 15,
    backgroundColor: '#fafafa',
  },
  inputMultiline: { minHeight: 100, textAlignVertical: 'top' },
  switchRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 20,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderColor: '#f0f0f0',
  },
  switchDesc: { fontSize: 12, color: '#999', marginTop: 2 },
  button: {
    backgroundColor: '#000',
    borderRadius: 10,
    padding: 16,
    alignItems: 'center',
    marginTop: 32,
  },
  buttonDisabled: { opacity: 0.6 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '700' },
});
