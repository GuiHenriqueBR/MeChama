import React, { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  addScheduleSlot,
  deleteScheduleSlot,
  getMyScheduleSlots,
  type ScheduleSlot,
} from '../../api/appointments';

const DAYS_PT: Record<string, string> = {
  Monday: 'Seg',
  Tuesday: 'Ter',
  Wednesday: 'Qua',
  Thursday: 'Qui',
  Friday: 'Sex',
  Saturday: 'Sáb',
  Sunday: 'Dom',
};

function formatTime(time: string): string {
  return time.slice(0, 5); // 'HH:mm:ss' → 'HH:mm'
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr + 'T00:00:00');
  const day = DAYS_PT[d.toLocaleDateString('en-US', { weekday: 'long' })] ?? '';
  return `${day}, ${d.toLocaleDateString('pt-BR')}`;
}

export default function ScheduleScreen() {
  const [slots, setSlots] = useState<ScheduleSlot[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialized, setInitialized] = useState(false);

  const loadSlots = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getMyScheduleSlots();
      setSlots(data);
    } catch {
      Alert.alert('Erro', 'Não foi possível carregar seus horários.');
    } finally {
      setLoading(false);
      setInitialized(true);
    }
  }, []);

  React.useEffect(() => {
    loadSlots();
  }, [loadSlots]);

  const handleAdd = useCallback(() => {
    // Em produção: abrir DateTimePicker ou modal de seleção
    // Aqui demonstramos com um horário fixo de exemplo
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0];

    Alert.alert(
      'Novo Horário',
      `Adicionar ${formatDate(dateStr)} das 09:00 às 10:00?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Adicionar',
          onPress: async () => {
            try {
              await addScheduleSlot({
                date: dateStr,
                startTime: '09:00:00',
                endTime: '10:00:00',
              });
              await loadSlots();
            } catch (err: unknown) {
              const msg = err instanceof Error ? err.message : 'Erro ao adicionar horário.';
              Alert.alert('Erro', msg);
            }
          },
        },
      ],
    );
  }, [loadSlots]);

  const handleDelete = useCallback((slot: ScheduleSlot) => {
    Alert.alert(
      'Remover Horário',
      `Remover ${formatDate(slot.date)} ${formatTime(slot.startTime)}–${formatTime(slot.endTime)}?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Remover',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteScheduleSlot(slot.id);
              setSlots(prev => prev.filter(s => s.id !== slot.id));
            } catch {
              Alert.alert('Erro', 'Não foi possível remover o horário.');
            }
          },
        },
      ],
    );
  }, []);

  if (loading && !initialized) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#7C3AED" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Minha Agenda</Text>
        <TouchableOpacity style={styles.addBtn} onPress={handleAdd}>
          <Text style={styles.addBtnText}>+ Horário</Text>
        </TouchableOpacity>
      </View>

      {slots.length === 0 ? (
        <View style={styles.center}>
          <Text style={styles.empty}>Nenhum horário cadastrado.</Text>
          <Text style={styles.emptySub}>Adicione horários para que clientes possam agendar.</Text>
        </View>
      ) : (
        <FlatList
          data={slots}
          keyExtractor={item => String(item.id)}
          contentContainerStyle={styles.list}
          refreshing={loading}
          onRefresh={loadSlots}
          renderItem={({ item }) => (
            <View style={[styles.card, !item.available && styles.cardBooked]}>
              <View style={styles.cardLeft}>
                <Text style={styles.cardDate}>{formatDate(item.date)}</Text>
                <Text style={styles.cardTime}>
                  {formatTime(item.startTime)} – {formatTime(item.endTime)}
                </Text>
                {!item.available && (
                  <Text style={styles.bookedBadge}>Reservado</Text>
                )}
              </View>
              {item.available && (
                <TouchableOpacity
                  style={styles.deleteBtn}
                  onPress={() => handleDelete(item)}
                >
                  <Text style={styles.deleteBtnText}>Remover</Text>
                </TouchableOpacity>
              )}
            </View>
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F9FAFB' },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E7EB',
  },
  title: { fontSize: 20, fontWeight: '700', color: '#111827' },
  addBtn: {
    backgroundColor: '#7C3AED',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 8,
  },
  addBtnText: { color: '#fff', fontWeight: '600', fontSize: 14 },
  list: { padding: 16, gap: 10 },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  cardBooked: { opacity: 0.6 },
  cardLeft: { gap: 4 },
  cardDate: { fontSize: 14, fontWeight: '600', color: '#374151' },
  cardTime: { fontSize: 16, fontWeight: '700', color: '#111827' },
  bookedBadge: {
    fontSize: 12,
    color: '#DC2626',
    fontWeight: '600',
    marginTop: 2,
  },
  deleteBtn: {
    borderWidth: 1,
    borderColor: '#DC2626',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
  },
  deleteBtnText: { color: '#DC2626', fontWeight: '600', fontSize: 13 },
  empty: { fontSize: 16, fontWeight: '600', color: '#374151', marginBottom: 8 },
  emptySub: { fontSize: 14, color: '#6B7280', textAlign: 'center' },
});
