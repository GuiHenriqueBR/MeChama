import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  Alert,
  TouchableOpacity,
  FlatList,
} from 'react-native';
import { getProviderProfile, ProviderProfileResponse, PortfolioItem } from '../../api/providers';

/**
 * Tela pública de detalhe do prestador.
 * Exibe: foto, bio, especialidades, localização, avaliação média e portfólio.
 *
 * Esta tela será enriquecida na Tarefa 3 com a lista de serviços ofertados
 * e na Tarefa 8 com as avaliações recentes.
 *
 * Props:
 * - route.params.userId: ID do prestador a exibir
 * - navigation: para navegar de volta ou para tela de agendamento (Tarefa 5)
 */
interface Props {
  route?: { params: { userId: number } };
  navigation?: any;
}

export default function ProviderDetailScreen({ route, navigation }: Props) {
  const userId = route?.params?.userId ?? 1;
  const [profile, setProfile] = useState<ProviderProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProfile();
  }, [userId]);

  const loadProfile = async () => {
    try {
      const data = await getProviderProfile(userId);
      setProfile(data);
    } catch {
      Alert.alert('Erro', 'Não foi possível carregar o perfil do prestador.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#000" />
      </View>
    );
  }

  if (!profile) {
    return (
      <View style={styles.centered}>
        <Text style={styles.emptyText}>Perfil não encontrado.</Text>
      </View>
    );
  }

  const renderStars = (rating: number) => {
    const filled = Math.round(rating);
    return '★'.repeat(filled) + '☆'.repeat(5 - filled);
  };

  const renderPortfolioItem = ({ item }: { item: PortfolioItem }) => (
    <View style={styles.portfolioCard}>
      {item.photoUrl ? (
        <Image source={{ uri: item.photoUrl }} style={styles.portfolioImage} resizeMode="cover" />
      ) : (
        <View style={[styles.portfolioImage, styles.portfolioPlaceholder]}>
          <Text style={styles.placeholderText}>📷</Text>
        </View>
      )}
      <Text style={styles.portfolioTitle}>{item.title}</Text>
      {item.description ? (
        <Text style={styles.portfolioDesc} numberOfLines={2}>{item.description}</Text>
      ) : null}
    </View>
  );

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Header com foto e nome */}
      <View style={styles.header}>
        {profile.avatarUrl ? (
          <Image source={{ uri: profile.avatarUrl }} style={styles.avatar} />
        ) : (
          <View style={[styles.avatar, styles.avatarPlaceholder]}>
            <Text style={styles.avatarInitial}>
              {profile.userName.charAt(0).toUpperCase()}
            </Text>
          </View>
        )}
        <Text style={styles.name}>{profile.userName}</Text>

        {/* Localização */}
        {(profile.city || profile.neighborhood) && (
          <Text style={styles.location}>
            📍 {[profile.neighborhood, profile.city].filter(Boolean).join(', ')}
          </Text>
        )}

        {/* Disponibilidade */}
        <View style={[styles.badge, profile.available ? styles.badgeGreen : styles.badgeGray]}>
          <Text style={styles.badgeText}>
            {profile.available ? 'Disponível' : 'Indisponível'}
          </Text>
        </View>
      </View>

      {/* Avaliação */}
      {profile.totalReviews > 0 && (
        <View style={styles.section}>
          <Text style={styles.stars}>{renderStars(profile.avgRating)}</Text>
          <Text style={styles.ratingText}>
            {profile.avgRating.toFixed(1)} ({profile.totalReviews}{' '}
            {profile.totalReviews === 1 ? 'avaliação' : 'avaliações'})
          </Text>
        </View>
      )}

      {/* Bio */}
      {profile.bio && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Sobre</Text>
          <Text style={styles.bioText}>{profile.bio}</Text>
        </View>
      )}

      {/* Especialidades */}
      {profile.specialties?.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Especialidades</Text>
          <View style={styles.tagsRow}>
            {profile.specialties.map((spec, i) => (
              <View key={i} style={styles.tag}>
                <Text style={styles.tagText}>{spec}</Text>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* Experiência */}
      {profile.experienceYears != null && (
        <View style={styles.infoRow}>
          <Text style={styles.infoIcon}>🏆</Text>
          <Text style={styles.infoText}>{profile.experienceYears} anos de experiência</Text>
        </View>
      )}

      {/* Portfólio */}
      {profile.portfolioItems?.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Portfólio</Text>
          <FlatList
            data={profile.portfolioItems}
            renderItem={renderPortfolioItem}
            keyExtractor={(item) => item.id.toString()}
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={{ gap: 12 }}
          />
        </View>
      )}

      {/* Botão de contratar — ativado na Tarefa 5 (Agendamento) */}
      <TouchableOpacity
        style={styles.contractButton}
        onPress={() =>
          Alert.alert('Em breve', 'Agendamento disponível na próxima versão!')
        }>
        <Text style={styles.contractButtonText}>Contratar prestador</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  emptyText: { color: '#999', fontSize: 16 },
  header: { alignItems: 'center', padding: 24, borderBottomWidth: 1, borderColor: '#f0f0f0' },
  avatar: { width: 90, height: 90, borderRadius: 45, marginBottom: 12 },
  avatarPlaceholder: { backgroundColor: '#111', justifyContent: 'center', alignItems: 'center' },
  avatarInitial: { color: '#fff', fontSize: 36, fontWeight: 'bold' },
  name: { fontSize: 22, fontWeight: 'bold', color: '#111' },
  location: { color: '#666', marginTop: 4, fontSize: 14 },
  badge: { marginTop: 10, paddingHorizontal: 12, paddingVertical: 4, borderRadius: 20 },
  badgeGreen: { backgroundColor: '#e6f4ea' },
  badgeGray: { backgroundColor: '#f0f0f0' },
  badgeText: { fontSize: 12, fontWeight: '600', color: '#333' },
  section: { padding: 16, borderBottomWidth: 1, borderColor: '#f5f5f5' },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: '#111', marginBottom: 10 },
  stars: { fontSize: 20, color: '#f5a623', letterSpacing: 2 },
  ratingText: { color: '#555', marginTop: 4, fontSize: 13 },
  bioText: { color: '#444', lineHeight: 22, fontSize: 15 },
  tagsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  tag: { backgroundColor: '#f0f0f0', borderRadius: 20, paddingHorizontal: 12, paddingVertical: 5 },
  tagText: { fontSize: 13, color: '#333' },
  infoRow: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 12 },
  infoIcon: { fontSize: 18, marginRight: 8 },
  infoText: { fontSize: 14, color: '#555' },
  portfolioCard: { width: 160 },
  portfolioImage: { width: 160, height: 120, borderRadius: 10, marginBottom: 6 },
  portfolioPlaceholder: { backgroundColor: '#f0f0f0', justifyContent: 'center', alignItems: 'center' },
  placeholderText: { fontSize: 30 },
  portfolioTitle: { fontSize: 13, fontWeight: '600', color: '#222' },
  portfolioDesc: { fontSize: 12, color: '#888', marginTop: 2 },
  contractButton: {
    backgroundColor: '#000',
    margin: 20,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  contractButtonText: { color: '#fff', fontSize: 16, fontWeight: '700' },
});
