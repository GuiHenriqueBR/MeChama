import React, { useState, useCallback, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  StyleSheet,
  ActivityIndicator,
  TouchableOpacity,
  Keyboard,
} from 'react-native';
import { SearchResult, SearchParams, SearchPage, searchServices } from '../../api/search';
import SearchResultCard from '../../components/SearchResultCard';
import SearchFilterPanel, { SearchFilters } from './SearchFilterPanel';

/**
 * Tela de busca e descoberta — Tarefa 4.
 *
 * Fluxo:
 *  1. Usuário digita na barra de busca (debounce de 400ms)
 *  2. Painél de filtros opcional (SearchFilterPanel — implementado pelo desenvolvedor)
 *  3. Resultados ranqueados por score multi-critério exibidos em lista paginada
 *  4. Paginação infinita (FlatList onEndReached) — carrega próxima página ao rolar
 *
 * Props:
 *  - navigation: para navegar para ProviderDetailScreen ao pressionar um card
 *
 * Integrações:
 *  - SearchResultCard (T4): card de cada resultado
 *  - SearchFilterPanel (T4): filtros de categoria, preço, cidade, avaliação
 *  - ProviderDetailScreen (T2/T3): destino ao pressionar um prestador
 *  - Tarefa 5 (Agendamento): botão "Agendar" nos cards (futuro)
 */

interface Props {
  navigation?: any;
}

const DEBOUNCE_MS = 400;

export default function SearchScreen({ navigation }: Props) {
  const [query, setQuery] = useState('');
  const [filters, setFilters] = useState<SearchFilters>({});
  const [showFilters, setShowFilters] = useState(false);
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [isLastPage, setIsLastPage] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ─── Busca ────────────────────────────────────────────────────────────────

  const buildParams = useCallback(
    (q: string, filts: SearchFilters, page: number): SearchParams => ({
      q: q || undefined,
      page,
      size: 10,
      categoryId: filts.categoryId,
      city: filts.city || undefined,
      minRating: filts.minRating,
      maxPrice: filts.maxPrice,
      minPrice: filts.minPrice,
    }),
    [],
  );

  const runSearch = useCallback(
    async (q: string, filts: SearchFilters, page = 0) => {
      if (page === 0) {
        setLoading(true);
        setResults([]);
        setHasSearched(true);
      } else {
        setLoadingMore(true);
      }

      try {
        const data: SearchPage = await searchServices(buildParams(q, filts, page));
        setResults(prev => (page === 0 ? data.content : [...prev, ...data.content]));
        setCurrentPage(data.number);
        setIsLastPage(data.last);
      } catch {
        // Erros silenciosos aqui — exibir snackbar na versão final
      } finally {
        setLoading(false);
        setLoadingMore(false);
      }
    },
    [buildParams],
  );

  // Debounce da barra de texto
  const handleQueryChange = (text: string) => {
    setQuery(text);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => runSearch(text, filters, 0), DEBOUNCE_MS);
  };

  // Filtros aplicados pelo SearchFilterPanel
  const handleApplyFilters = (newFilters: SearchFilters) => {
    setFilters(newFilters);
    setShowFilters(false);
    runSearch(query, newFilters, 0);
  };

  // Paginação infinita
  const handleLoadMore = () => {
    if (!loadingMore && !isLastPage && hasSearched) {
      runSearch(query, filters, currentPage + 1);
    }
  };

  // Navegar para detalhe do prestador
  const handleCardPress = (item: SearchResult) => {
    Keyboard.dismiss();
    navigation?.navigate('ProviderDetail', { userId: item.providerUserId });
  };

  // Contagem de filtros ativos (badge no botão)
  const activeFilterCount = Object.values(filters).filter(v => v !== undefined && v !== null).length;

  // ─── Render ────────────────────────────────────────────────────────────────

  const renderEmpty = () => {
    if (!hasSearched) {
      return (
        <View style={styles.emptyState}>
          <Text style={styles.emptyIcon}>🔍</Text>
          <Text style={styles.emptyTitle}>Encontre o profissional certo</Text>
          <Text style={styles.emptySubtitle}>
            Digite um serviço, profissão ou categoria para começar
          </Text>
        </View>
      );
    }
    return (
      <View style={styles.emptyState}>
        <Text style={styles.emptyIcon}>😕</Text>
        <Text style={styles.emptyTitle}>Nenhum resultado encontrado</Text>
        <Text style={styles.emptySubtitle}>
          Tente outros termos ou remova alguns filtros
        </Text>
      </View>
    );
  };

  const renderFooter = () => {
    if (!loadingMore) return null;
    return (
      <View style={styles.footerLoader}>
        <ActivityIndicator size="small" color="#000" />
      </View>
    );
  };

  return (
    <View style={styles.container}>
      {/* Barra de busca + botão de filtros */}
      <View style={styles.searchBar}>
        <View style={styles.inputWrapper}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={styles.input}
            placeholder="Buscar serviço ou profissional..."
            placeholderTextColor="#999"
            value={query}
            onChangeText={handleQueryChange}
            returnKeyType="search"
            onSubmitEditing={() => {
              if (debounceRef.current) clearTimeout(debounceRef.current);
              runSearch(query, filters, 0);
            }}
            autoCorrect={false}
          />
          {query.length > 0 && (
            <TouchableOpacity onPress={() => handleQueryChange('')}>
              <Text style={styles.clearIcon}>✕</Text>
            </TouchableOpacity>
          )}
        </View>

        <TouchableOpacity
          style={[styles.filterButton, activeFilterCount > 0 && styles.filterButtonActive]}
          onPress={() => setShowFilters(!showFilters)}>
          <Text style={styles.filterIcon}>⚙</Text>
          {activeFilterCount > 0 && (
            <View style={styles.filterBadge}>
              <Text style={styles.filterBadgeText}>{activeFilterCount}</Text>
            </View>
          )}
        </TouchableOpacity>
      </View>

      {/* Painel de filtros (colapsável) */}
      {showFilters && (
        <SearchFilterPanel
          initialFilters={filters}
          onApply={handleApplyFilters}
          onClose={() => setShowFilters(false)}
        />
      )}

      {/* Contador de resultados */}
      {hasSearched && !loading && results.length > 0 && (
        <Text style={styles.resultCount}>
          {results.length} resultado{results.length !== 1 ? 's' : ''}
          {activeFilterCount > 0 ? ' com filtros' : ''}
        </Text>
      )}

      {/* Lista de resultados */}
      {loading ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#000" />
        </View>
      ) : (
        <FlatList
          data={results}
          keyExtractor={item => item.serviceId.toString()}
          renderItem={({ item }) => (
            <SearchResultCard item={item} onPress={handleCardPress} />
          )}
          ListEmptyComponent={renderEmpty}
          ListFooterComponent={renderFooter}
          onEndReached={handleLoadMore}
          onEndReachedThreshold={0.3}
          contentContainerStyle={results.length === 0 ? styles.emptyContainer : styles.listContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f8f8' },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderColor: '#f0f0f0',
    gap: 8,
  },
  inputWrapper: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f2f2f2',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 8,
  },
  searchIcon: { fontSize: 16, marginRight: 6 },
  input: {
    flex: 1,
    fontSize: 15,
    color: '#111',
    padding: 0,
  },
  clearIcon: { fontSize: 14, color: '#999', paddingLeft: 6 },
  filterButton: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: '#f2f2f2',
    justifyContent: 'center',
    alignItems: 'center',
  },
  filterButtonActive: { backgroundColor: '#000' },
  filterIcon: { fontSize: 18 },
  filterBadge: {
    position: 'absolute',
    top: 4,
    right: 4,
    backgroundColor: '#e00',
    borderRadius: 8,
    width: 16,
    height: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  filterBadgeText: { color: '#fff', fontSize: 10, fontWeight: 'bold' },
  resultCount: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    fontSize: 13,
    color: '#888',
  },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  listContent: { paddingVertical: 8 },
  emptyContainer: { flex: 1 },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  emptyIcon: { fontSize: 48, marginBottom: 16 },
  emptyTitle: { fontSize: 18, fontWeight: '700', color: '#111', marginBottom: 8, textAlign: 'center' },
  emptySubtitle: { fontSize: 14, color: '#888', textAlign: 'center', lineHeight: 20 },
  footerLoader: { padding: 16, alignItems: 'center' },
});
