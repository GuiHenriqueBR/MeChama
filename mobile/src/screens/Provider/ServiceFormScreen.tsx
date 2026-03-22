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
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import {
  getCategories,
  createService,
  updateService,
  ServiceCategory,
  ServiceOfferingRequest,
  ServiceOfferingResponse,
} from '../../api/services';

/**
 * Formulário de criação e edição de serviço.
 *
 * Props via route.params:
 * - service: ServiceOfferingResponse | null
 *   Se null → modo criação; se preenchido → modo edição.
 *
 * Validações críticas:
 * - Preço mínimo por categoria (exibido na UI para o prestador)
 * - Backend também valida e retorna erro claro se abaixo do piso
 */
interface Props {
  route?: { params: { service: ServiceOfferingResponse | null } };
  navigation?: any;
}

export default function ServiceFormScreen({ route, navigation }: Props) {
  const existingService = route?.params?.service ?? null;
  const isEditing = existingService !== null;

  const [categories, setCategories] = useState<ServiceCategory[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<ServiceCategory | null>(null);
  const [showCategoryPicker, setShowCategoryPicker] = useState(false);

  const [title, setTitle] = useState(existingService?.title ?? '');
  const [description, setDescription] = useState(existingService?.description ?? '');
  const [basePrice, setBasePrice] = useState(existingService?.basePrice?.toString() ?? '');
  const [durationMinutes, setDurationMinutes] = useState(
    existingService?.durationMinutes?.toString() ?? '',
  );
  const [whatIsIncluded, setWhatIsIncluded] = useState(existingService?.whatIsIncluded ?? '');
  const [whatIsNotIncluded, setWhatIsNotIncluded] = useState(
    existingService?.whatIsNotIncluded ?? '',
  );

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await getCategories();
      setCategories(data);

      // Pré-seleciona a categoria se for edição
      if (existingService) {
        const cat = data.find((c) => c.id === existingService.categoryId) ?? null;
        setSelectedCategory(cat);
      }
    } catch {
      Alert.alert('Erro', 'Não foi possível carregar as categorias.');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!selectedCategory) {
      Alert.alert('Atenção', 'Selecione uma categoria.');
      return;
    }
    if (!title.trim()) {
      Alert.alert('Atenção', 'Informe o título do serviço.');
      return;
    }
    const price = parseFloat(basePrice.replace(',', '.'));
    if (isNaN(price) || price <= 0) {
      Alert.alert('Atenção', 'Informe um preço válido.');
      return;
    }
    if (price < selectedCategory.minPrice) {
      Alert.alert(
        'Preço abaixo do mínimo',
        `O preço mínimo para "${selectedCategory.name}" é R$ ${selectedCategory.minPrice.toFixed(2)}.`,
      );
      return;
    }

    const request: ServiceOfferingRequest = {
      categoryId: selectedCategory.id,
      title: title.trim(),
      description: description.trim() || undefined,
      basePrice: price,
      durationMinutes: durationMinutes ? parseInt(durationMinutes, 10) : undefined,
      whatIsIncluded: whatIsIncluded.trim() || undefined,
      whatIsNotIncluded: whatIsNotIncluded.trim() || undefined,
    };

    setSaving(true);
    try {
      if (isEditing && existingService) {
        await updateService(existingService.id, request);
      } else {
        await createService(request);
      }
      Alert.alert(
        'Sucesso',
        isEditing ? 'Serviço atualizado!' : 'Serviço criado com sucesso!',
        [{ text: 'OK', onPress: () => navigation?.goBack() }],
      );
    } catch (error: any) {
      const message = error?.response?.data?.message || 'Falha ao salvar o serviço.';
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
        <Text style={styles.title}>{isEditing ? 'Editar serviço' : 'Novo serviço'}</Text>

        {/* Seletor de categoria */}
        <Text style={styles.label}>Categoria *</Text>
        <TouchableOpacity
          style={styles.categorySelector}
          onPress={() => setShowCategoryPicker(!showCategoryPicker)}>
          <Text style={styles.categorySelectorText}>
            {selectedCategory
              ? `${selectedCategory.icon}  ${selectedCategory.name}`
              : 'Selecione uma categoria'}
          </Text>
          <Text style={styles.chevron}>{showCategoryPicker ? '▲' : '▼'}</Text>
        </TouchableOpacity>

        {showCategoryPicker && (
          <View style={styles.categoryList}>
            {categories.map((cat) => (
              <TouchableOpacity
                key={cat.id}
                style={[
                  styles.categoryOption,
                  selectedCategory?.id === cat.id && styles.categoryOptionSelected,
                ]}
                onPress={() => {
                  setSelectedCategory(cat);
                  setShowCategoryPicker(false);
                }}>
                <Text style={styles.categoryOptionText}>
                  {cat.icon}  {cat.name}
                </Text>
                <Text style={styles.categoryMinPrice}>
                  Mín: R$ {cat.minPrice.toFixed(2)}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        )}

        {/* Alerta de preço mínimo */}
        {selectedCategory && (
          <View style={styles.minPriceAlert}>
            <Text style={styles.minPriceAlertText}>
              💡 Preço mínimo para {selectedCategory.name}: R$ {selectedCategory.minPrice.toFixed(2)}
            </Text>
          </View>
        )}

        {/* Título */}
        <Text style={styles.label}>Título do serviço *</Text>
        <TextInput
          style={styles.input}
          value={title}
          onChangeText={setTitle}
          placeholder="Ex.: Instalação elétrica residencial"
          maxLength={150}
        />

        {/* Preço */}
        <Text style={styles.label}>Preço base (R$) *</Text>
        <TextInput
          style={styles.input}
          value={basePrice}
          onChangeText={setBasePrice}
          placeholder="Ex.: 150,00"
          keyboardType="decimal-pad"
        />

        {/* Duração */}
        <Text style={styles.label}>Duração estimada (minutos)</Text>
        <TextInput
          style={styles.input}
          value={durationMinutes}
          onChangeText={setDurationMinutes}
          placeholder="Ex.: 120 (= 2 horas)"
          keyboardType="numeric"
        />

        {/* Descrição */}
        <Text style={styles.label}>Descrição</Text>
        <TextInput
          style={[styles.input, styles.inputMultiline]}
          value={description}
          onChangeText={setDescription}
          placeholder="Descreva o serviço em detalhes..."
          multiline
          numberOfLines={4}
          maxLength={2000}
        />

        {/* O que está incluso */}
        <Text style={styles.label}>O que está incluso</Text>
        <TextInput
          style={[styles.input, styles.inputMultiline]}
          value={whatIsIncluded}
          onChangeText={setWhatIsIncluded}
          placeholder="Ex.: Diagnóstico completo, instalação e teste de funcionamento"
          multiline
          numberOfLines={3}
          maxLength={1000}
        />

        {/* O que NÃO está incluso */}
        <Text style={styles.label}>O que NÃO está incluso</Text>
        <TextInput
          style={[styles.input, styles.inputMultiline]}
          value={whatIsNotIncluded}
          onChangeText={setWhatIsNotIncluded}
          placeholder="Ex.: Materiais, peças de reposição, deslocamento acima de 20 km"
          multiline
          numberOfLines={3}
          maxLength={1000}
        />

        <TouchableOpacity
          style={[styles.button, saving && styles.buttonDisabled]}
          onPress={handleSave}
          disabled={saving}>
          {saving ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <Text style={styles.buttonText}>
              {isEditing ? 'Salvar alterações' : 'Criar serviço'}
            </Text>
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
  inputMultiline: { minHeight: 90, textAlignVertical: 'top' },
  categorySelector: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fafafa',
  },
  categorySelectorText: { fontSize: 15, color: '#333' },
  chevron: { fontSize: 12, color: '#888' },
  categoryList: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 10,
    marginTop: 4,
    maxHeight: 260,
    overflow: 'hidden',
  },
  categoryOption: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderColor: '#f0f0f0',
  },
  categoryOptionSelected: { backgroundColor: '#f0f0f0' },
  categoryOptionText: { fontSize: 14, color: '#222' },
  categoryMinPrice: { fontSize: 12, color: '#888' },
  minPriceAlert: {
    backgroundColor: '#fffde7',
    borderRadius: 8,
    padding: 10,
    marginTop: 8,
  },
  minPriceAlertText: { fontSize: 13, color: '#795548' },
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
