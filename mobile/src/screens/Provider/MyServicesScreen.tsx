import React, { useCallback, useState } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { getMyServices, toggleService, deleteService, ServiceOfferingResponse } from '../../api/services';

/**
 * Tela de gerenciamento dos serviços do prestador.
 * Lista todos os serviços (ativos e inativos) com opções de:
 * - Editar
 * - Ativar/Desativar
 * - Excluir
 * - Criar novo
 */
interface Props {
  navigation?: any;
}

export default function MyServicesScreen({ navigation }: Props) {
  const [services, setServices] = useState<ServiceOfferingResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Recarrega sempre que a tela recebe foco (ex.: após salvar na ServiceFormScreen)
  useFocusEffect(
    useCallback(() => {
      loadServices();
    }, []),
  );

  const loadServices = async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    try {
      const data = await getMyServices();
      setServices(data);
    } catch {
      Alert.alert('Erro', 'Não foi possível carregar seus serviços.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleToggle = async (service: ServiceOfferingResponse) => {
    try {
      const updated = await toggleService(service.id);
      setServices((prev) =>
        prev.map((s) => (s.id === updated.id ? updated : s)),
      );
    } catch {
      Alert.alert('Erro', 'Não foi possível alterar o status do serviço.');
    }
  };

  const handleDelete = (service: ServiceOfferingResponse) => {
    Alert.alert(
      'Excluir serviço',
      `Tem certeza que deseja excluir "${service.title}"? Esta ação não pode ser desfeita.`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Excluir',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteService(service.id);
              setServices((prev) => prev.filter((s) => s.id !== service.id));
            } catch {
              Alert.alert('Erro', 'Não foi possível excluir o serviço.');
            }
          },
        },
      ],
    );
  };

  const formatPrice = (price: number) =>
    price.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  const formatDuration = (minutes?: number) => {
    if (!minutes) return null;
    if (minutes < 60) return `${minutes} min`;
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return m > 0 ? `${h}h${m}min` : `${h}h`;
  };

  const renderItem = ({ item }: { item: ServiceOfferingResponse }) => (
    <View style={[styles.card, !item.active && styles.cardInactive]}>
      {/* Cabeçalho do card */}
      <View style={styles.cardHeader}>
        <View style={styles.categoryBadge}>
          <Text style={styles.categoryIcon}>{item.categoryIcon}</Text>
          <Text style={styles.categoryName}>{item.categoryName}</Text>
        </View>
        <View style={[styles.statusBadge, item.active ? styles.statusActive : styles.statusInactive]}>
          <Text style={styles.statusText}>{item.active ? 'Ativo' : 'Inativo'}</Text>
        </View>
      </View>

      {/* Título e preço */}
      <Text style={styles.serviceTitle}>{item.title}</Text>
      <Text style={styles.servicePrice}>{formatPrice(item.basePrice)}</Text>

      {/* Duração */}
      {item.durationMinutes && (
        <Text style={styles.serviceDuration}>⏱ {formatDuration(item.durationMinutes)}</Text>
      )}

      {/* Ações */}
      <View style={styles.actions}>
        <TouchableOpacity
          style={styles.actionBtn}
          onPress={() => navigation?.navigate('ServiceForm', { service: item })}>
          <Text style={styles.actionBtnText}>✏️ Editar</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.actionBtn, styles.actionBtnToggle]}
          onPress={() => handleToggle(item)}>
          <Text style={styles.actionBtnText}>
            {item.active ? '🔴 Desativar' : '🟢 Ativar'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.actionBtn, styles.actionBtnDelete]}
          onPress={() => handleDelete(item)}>
          <Text style={[styles.actionBtnText, styles.actionBtnDeleteText]}>🗑</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#000" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={services}
        renderItem={renderItem}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => loadServices(true)} />
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyIcon}>🛠️</Text>
            <Text style={styles.emptyTitle}>Nenhum serviço cadastrado</Text>
            <Text style={styles.emptyDesc}>
              Adicione serviços para aparecer nas buscas e receber pedidos.
            </Text>
          </View>
        }
      />

      {/* FAB — botão flutuante para criar novo serviço */}
      <TouchableOpacity
        style={styles.fab}
        onPress={() => navigation?.navigate('ServiceForm', { service: null })}>
        <Text style={styles.fabText}>+ Novo serviço</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  list: { padding: 16, paddingBottom: 100 },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  cardInactive: { opacity: 0.6 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  categoryBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    borderRadius: 20,
    paddingHorizontal: 10,
    paddingVertical: 3,
  },
  categoryIcon: { fontSize: 14, marginRight: 4 },
  categoryName: { fontSize: 12, color: '#555' },
  statusBadge: { borderRadius: 20, paddingHorizontal: 10, paddingVertical: 3 },
  statusActive: { backgroundColor: '#e6f4ea' },
  statusInactive: { backgroundColor: '#fce8e6' },
  statusText: { fontSize: 11, fontWeight: '600', color: '#333' },
  serviceTitle: { fontSize: 17, fontWeight: '700', color: '#111', marginBottom: 4 },
  servicePrice: { fontSize: 18, fontWeight: '800', color: '#000' },
  serviceDuration: { fontSize: 13, color: '#888', marginTop: 4 },
  actions: { flexDirection: 'row', gap: 8, marginTop: 14 },
  actionBtn: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    paddingVertical: 8,
    alignItems: 'center',
  },
  actionBtnToggle: { backgroundColor: '#fafafa' },
  actionBtnDelete: { flex: 0, width: 40, backgroundColor: '#fff0f0' },
  actionBtnText: { fontSize: 13, fontWeight: '600', color: '#333' },
  actionBtnDeleteText: { color: '#c00' },
  empty: { alignItems: 'center', paddingTop: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyTitle: { fontSize: 18, fontWeight: '700', color: '#333' },
  emptyDesc: { fontSize: 14, color: '#888', textAlign: 'center', marginTop: 6, paddingHorizontal: 24 },
  fab: {
    position: 'absolute',
    bottom: 24,
    left: 24,
    right: 24,
    backgroundColor: '#000',
    borderRadius: 14,
    padding: 16,
    alignItems: 'center',
  },
  fabText: { color: '#fff', fontSize: 16, fontWeight: '700' },
});
