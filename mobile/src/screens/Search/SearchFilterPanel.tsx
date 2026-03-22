import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  TextInput,
} from 'react-native';
import { ServiceCategory, getCategories } from '../../api/services';

// ─── Tipos ───────────────────────────────────────────────────────────────────

export interface SearchFilters {
  categoryId?: number;
  city?: string;
  minRating?: number;
  maxPrice?: number;
  minPrice?: number;
}

interface Props {
  initialFilters: SearchFilters;
  onApply: (filters: SearchFilters) => void;
  onClose: () => void;
}

// ─── Componente ──────────────────────────────────────────────────────────────

export default function SearchFilterPanel({ initialFilters, onApply, onClose }: Props) {
  const [categories, setCategories] = useState<ServiceCategory[]>([]);

  // Campos de filtro em estado local
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | undefined>(
    initialFilters.categoryId,
  );
  const [minRating, setMinRating] = useState<number | undefined>(initialFilters.minRating);
  const [city, setCity] = useState(initialFilters.city ?? '');
  const [minPriceText, setMinPriceText] = useState(
    initialFilters.minPrice != null ? String(initialFilters.minPrice) : '',
  );
  const [maxPriceText, setMaxPriceText] = useState(
    initialFilters.maxPrice != null ? String(initialFilters.maxPrice) : '',
  );

  // Validação de preço em tempo real — derivada do estado, sem useEffect
  const minPriceVal = minPriceText !== '' ? parseFloat(minPriceText) : undefined;
  const maxPriceVal = maxPriceText !== '' ? parseFloat(maxPriceText) : undefined;
  const priceError =
    minPriceVal != null &&
    maxPriceVal != null &&
    !isNaN(minPriceVal) &&
    !isNaN(maxPriceVal) &&
    minPriceVal > maxPriceVal;

  // Carrega categorias da API ao montar — público, sem token
  useEffect(() => {
    getCategories().then(setCategories).catch(() => {});
  }, []);

  // ─── Handlers ──────────────────────────────────────────────────────────────

  const handleCategoryChip = (id: number) => {
    // Toca no chip já selecionado → deseleciona
    setSelectedCategoryId(prev => (prev === id ? undefined : id));
  };

  const handleStar = (star: number) => {
    // Toca na estrela já selecionada → limpa
    setMinRating(prev => (prev === star ? undefined : star));
  };

  const buildFilters = (): SearchFilters => ({
    categoryId: selectedCategoryId,
    city: city.trim() || undefined,
    minRating,
    minPrice: minPriceVal != null && !isNaN(minPriceVal) ? minPriceVal : undefined,
    maxPrice: maxPriceVal != null && !isNaN(maxPriceVal) ? maxPriceVal : undefined,
  });

  const handleApply = () => {
    if (!priceError) onApply(buildFilters());
  };

  const handleClear = () => {
    setSelectedCategoryId(undefined);
    setMinRating(undefined);
    setCity('');
    setMinPriceText('');
    setMaxPriceText('');
    onApply({});
  };

  // ─── Render ────────────────────────────────────────────────────────────────

  return (
    <View style={styles.container}>
      {/* Cabeçalho */}
      <View style={styles.header}>
        <Text style={styles.title}>Filtros</Text>
        <TouchableOpacity onPress={onClose} style={styles.closeButton}>
          <Text style={styles.closeText}>✕</Text>
        </TouchableOpacity>
      </View>

      <ScrollView style={styles.body} showsVerticalScrollIndicator={false}>

        {/* ── Categoria ─────────────────────────────────────────────────────── */}
        {categories.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionLabel}>Categoria</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsScroll}>
              {categories.map(cat => {
                const active = selectedCategoryId === cat.id;
                return (
                  <TouchableOpacity
                    key={cat.id}
                    style={[styles.chip, active && styles.chipActive]}
                    onPress={() => handleCategoryChip(cat.id)}
                    activeOpacity={0.75}>
                    {cat.icon ? (
                      <Text style={styles.chipIcon}>{cat.icon}</Text>
                    ) : null}
                    <Text style={[styles.chipText, active && styles.chipTextActive]}>
                      {cat.name}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </ScrollView>
          </View>
        )}

        {/* ── Avaliação mínima ──────────────────────────────────────────────── */}
        <View style={styles.section}>
          <Text style={styles.sectionLabel}>Avaliação mínima</Text>
          <View style={styles.starsRow}>
            {[1, 2, 3, 4, 5].map(star => {
              const filled = minRating != null && star <= minRating;
              return (
                <TouchableOpacity
                  key={star}
                  onPress={() => handleStar(star)}
                  style={styles.starButton}
                  activeOpacity={0.7}>
                  <Text style={[styles.star, filled && styles.starFilled]}>★</Text>
                </TouchableOpacity>
              );
            })}
            {minRating != null && (
              <Text style={styles.ratingHint}>{minRating}+ estrelas</Text>
            )}
          </View>
        </View>

        {/* ── Faixa de preço ────────────────────────────────────────────────── */}
        <View style={styles.section}>
          <Text style={styles.sectionLabel}>Faixa de preço (R$)</Text>
          <View style={styles.priceRow}>
            <TextInput
              style={[styles.priceInput, priceError && styles.priceInputError]}
              placeholder="Mínimo"
              placeholderTextColor="#bbb"
              value={minPriceText}
              onChangeText={setMinPriceText}
              keyboardType="decimal-pad"
            />
            <Text style={styles.priceSeparator}>—</Text>
            <TextInput
              style={[styles.priceInput, priceError && styles.priceInputError]}
              placeholder="Máximo"
              placeholderTextColor="#bbb"
              value={maxPriceText}
              onChangeText={setMaxPriceText}
              keyboardType="decimal-pad"
            />
          </View>
          {priceError && (
            <Text style={styles.priceError}>O valor mínimo não pode ser maior que o máximo</Text>
          )}
        </View>

        {/* ── Cidade ────────────────────────────────────────────────────────── */}
        <View style={[styles.section, styles.sectionLast]}>
          <Text style={styles.sectionLabel}>Cidade</Text>
          <TextInput
            style={styles.cityInput}
            placeholder="Ex.: São Paulo"
            placeholderTextColor="#bbb"
            value={city}
            onChangeText={setCity}
            autoCorrect={false}
          />
        </View>

      </ScrollView>

      {/* Rodapé */}
      <View style={styles.footer}>
        <TouchableOpacity style={styles.clearButton} onPress={handleClear}>
          <Text style={styles.clearText}>Limpar</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.applyButton, priceError && styles.applyButtonDisabled]}
          onPress={handleApply}
          disabled={priceError}>
          <Text style={styles.applyText}>Aplicar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

// ─── Estilos ─────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderColor: '#e8e8e8',
    maxHeight: 420,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderColor: '#f0f0f0',
  },
  title: { fontSize: 16, fontWeight: '700', color: '#111' },
  closeButton: { padding: 4 },
  closeText: { fontSize: 16, color: '#888' },

  body: { paddingHorizontal: 16 },

  section: {
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderColor: '#f4f4f4',
  },
  sectionLast: { borderBottomWidth: 0 },
  sectionLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: '#555',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 10,
  },

  // Chips de categoria
  chipsScroll: { flexDirection: 'row' },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: '#ddd',
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 7,
    marginRight: 8,
    backgroundColor: '#fff',
  },
  chipActive: {
    backgroundColor: '#111',
    borderColor: '#111',
  },
  chipIcon: { fontSize: 14, marginRight: 5 },
  chipText: { fontSize: 13, fontWeight: '600', color: '#333' },
  chipTextActive: { color: '#fff' },

  // Estrelas
  starsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  starButton: { padding: 4 },
  star: { fontSize: 30, color: '#ddd' },
  starFilled: { color: '#f5a623' },
  ratingHint: {
    marginLeft: 10,
    fontSize: 13,
    color: '#888',
    fontWeight: '500',
  },

  // Preço
  priceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  priceInput: {
    flex: 1,
    borderWidth: 1.5,
    borderColor: '#ddd',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 9,
    fontSize: 15,
    color: '#111',
  },
  priceInputError: { borderColor: '#e00' },
  priceSeparator: { fontSize: 16, color: '#999' },
  priceError: {
    marginTop: 6,
    fontSize: 12,
    color: '#e00',
  },

  // Cidade
  cityInput: {
    borderWidth: 1.5,
    borderColor: '#ddd',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 9,
    fontSize: 15,
    color: '#111',
  },

  // Rodapé
  footer: {
    flexDirection: 'row',
    gap: 10,
    padding: 14,
    borderTopWidth: 1,
    borderColor: '#f0f0f0',
  },
  clearButton: {
    flex: 1,
    padding: 12,
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: '#ddd',
    alignItems: 'center',
  },
  clearText: { fontSize: 14, color: '#555', fontWeight: '600' },
  applyButton: {
    flex: 2,
    padding: 12,
    borderRadius: 10,
    backgroundColor: '#000',
    alignItems: 'center',
  },
  applyButtonDisabled: { backgroundColor: '#aaa' },
  applyText: { fontSize: 14, color: '#fff', fontWeight: '700' },
});
