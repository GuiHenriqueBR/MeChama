import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  createAppointmentRequest,
  getProviderAvailableSlots,
  type ScheduleSlot,
} from '../../api/appointments';

interface Props {
  route: {
    params: {
      providerUserId: number;
      providerName: string;
      serviceId: number;
      serviceTitle: string;
    };
  };
  navigation: { goBack: () => void; navigate: (screen: string) => void };
}

function formatTime(time: string): string {
  return time.slice(0, 5);
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
  });
}

export default function BookingScreen({ route, navigation }: Props) {
  const { providerUserId, providerName, serviceId, serviceTitle } = route.params;

  const [slots, setSlots] = useState<ScheduleSlot[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<ScheduleSlot | null>(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getProviderAvailableSlots(providerUserId)
      .then(setSlots)
      .catch(() => Alert.alert('Erro', 'Não foi possível carregar os horários disponíveis.'))
      .finally(() => setLoading(false));
  }, [providerUserId]);

  const handleConfirm = useCallback(async () => {
    if (!selectedSlot) {
      Alert.alert('Selecione um horário', 'Escolha um horário disponível para prosseguir.');
      return;
    }

    setSubmitting(true);
    try {
      // Usa a data+hora de início do slot como proposedDatetime
      const proposedDatetime = `${selectedSlot.date}T${selectedSlot.startTime}`;

      await createAppointmentRequest({
        providerUserId,
        serviceId,
        scheduleId: selectedSlot.id,
        proposedDatetime,
        notes: notes.trim() || undefined,
      });

      Alert.alert(
        'Solicitação enviada!',
        `Sua solicitação de agendamento com ${providerName} foi enviada. Aguarde a confirmação.`,
        [{ text: 'OK', onPress: () => navigation.navigate('MyAppointments') }],
      );
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Erro ao enviar solicitação.';
      Alert.alert('Erro', msg);
    } finally {
      setSubmitting(false);
    }
  }, [selectedSlot, notes, providerUserId, serviceId, providerName, navigation]);

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#7C3AED" />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Resumo do serviço */}
      <View style={styles.serviceCard}>
        <Text style={styles.serviceLabel}>Serviço</Text>
        <Text style={styles.serviceTitle}>{serviceTitle}</Text>
        <Text style={styles.providerName}>com {providerName}</Text>
      </View>

      {/* Seleção de horário */}
      <Text style={styles.sectionTitle}>Horários Disponíveis</Text>

      {slots.length === 0 ? (
        <View style={styles.emptySlots}>
          <Text style={styles.emptyText}>
            Nenhum horário disponível no momento.
          </Text>
          <Text style={styles.emptySubText}>
            Entre em contato com o prestador para combinar um horário.
          </Text>
        </View>
      ) : (
        <FlatList
          data={slots}
          keyExtractor={item => String(item.id)}
          scrollEnabled={false}
          contentContainerStyle={styles.slotList}
          renderItem={({ item }) => {
            const selected = selectedSlot?.id === item.id;
            return (
              <TouchableOpacity
                style={[styles.slotCard, selected && styles.slotCardSelected]}
                onPress={() => setSelectedSlot(item)}
                activeOpacity={0.7}
              >
                <Text style={[styles.slotDate, selected && styles.slotTextSelected]}>
                  {formatDate(item.date)}
                </Text>
                <Text style={[styles.slotTime, selected && styles.slotTextSelected]}>
                  {formatTime(item.startTime)} – {formatTime(item.endTime)}
                </Text>
                {selected && <Text style={styles.selectedCheck}>✓ Selecionado</Text>}
              </TouchableOpacity>
            );
          }}
        />
      )}

      {/* Observações */}
      <Text style={styles.sectionTitle}>Observações (opcional)</Text>
      <TextInput
        style={styles.notesInput}
        placeholder="Ex.: apartamento no 3º andar, sem elevador..."
        placeholderTextColor="#9CA3AF"
        multiline
        numberOfLines={3}
        value={notes}
        onChangeText={setNotes}
        maxLength={500}
      />

      {/* Botão confirmar */}
      <TouchableOpacity
        style={[styles.confirmBtn, (!selectedSlot || submitting) && styles.confirmBtnDisabled]}
        onPress={handleConfirm}
        disabled={!selectedSlot || submitting}
        activeOpacity={0.8}
      >
        {submitting ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.confirmBtnText}>Solicitar Agendamento</Text>
        )}
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F9FAFB' },
  content: { padding: 16, paddingBottom: 40 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },

  serviceCard: {
    backgroundColor: '#7C3AED',
    borderRadius: 12,
    padding: 16,
    marginBottom: 24,
  },
  serviceLabel: { fontSize: 12, color: '#DDD6FE', marginBottom: 4 },
  serviceTitle: { fontSize: 18, fontWeight: '700', color: '#fff' },
  providerName: { fontSize: 14, color: '#EDE9FE', marginTop: 4 },

  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 12,
    marginTop: 8,
  },

  slotList: { gap: 10, marginBottom: 24 },
  slotCard: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 14,
    borderWidth: 2,
    borderColor: '#E5E7EB',
  },
  slotCardSelected: {
    borderColor: '#7C3AED',
    backgroundColor: '#F5F3FF',
  },
  slotDate: { fontSize: 13, color: '#6B7280', marginBottom: 2 },
  slotTime: { fontSize: 16, fontWeight: '700', color: '#111827' },
  slotTextSelected: { color: '#7C3AED' },
  selectedCheck: { fontSize: 12, color: '#7C3AED', fontWeight: '600', marginTop: 4 },

  emptySlots: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 20,
    alignItems: 'center',
    marginBottom: 24,
  },
  emptyText: { fontSize: 15, fontWeight: '600', color: '#374151', marginBottom: 6 },
  emptySubText: { fontSize: 13, color: '#6B7280', textAlign: 'center' },

  notesInput: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 10,
    padding: 12,
    fontSize: 14,
    color: '#111827',
    textAlignVertical: 'top',
    minHeight: 80,
    marginBottom: 24,
  },

  confirmBtn: {
    backgroundColor: '#7C3AED',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
  },
  confirmBtnDisabled: { backgroundColor: '#C4B5FD' },
  confirmBtnText: { color: '#fff', fontSize: 16, fontWeight: '700' },
});
