import React from 'react';
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { SearchResult } from '../api/search';

interface Props {
  item: SearchResult;
  onPress: (item: SearchResult) => void;
}

/**
 * Card de resultado de busca.
 *
 * Exibe:
 * - Avatar + nome do prestador
 * - Título do serviço e categoria (com ícone)
 * - Preço base e duração estimada
 * - Avaliação média em estrelas e número de reviews
 * - Cidade do prestador
 *
 * Ao pressionar, navega para ProviderDetailScreen (Tarefa 2/3).
 *
 * Integrações futuras:
 * - Tarefa 5 (Agendamento): botão "Agendar" direto no card
 * - Tarefa 11 (Anúncios): badge "Destaque" quando item.score tiver componente featured > 0
 */
export default function SearchResultCard({ item, onPress }: Props) {
  const renderStars = (rating: number) => {
    const filled = Math.round(rating);
    return '★'.repeat(filled) + '☆'.repeat(5 - filled);
  };

  const formatPrice = (price: number) =>
    price.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  return (
    <TouchableOpacity style={styles.card} onPress={() => onPress(item)} activeOpacity={0.85}>
      {/* Header: avatar + prestador + localização */}
      <View style={styles.header}>
        {item.providerAvatarUrl ? (
          <Image source={{ uri: item.providerAvatarUrl }} style={styles.avatar} />
        ) : (
          <View style={[styles.avatar, styles.avatarPlaceholder]}>
            <Text style={styles.avatarInitial}>
              {item.providerName.charAt(0).toUpperCase()}
            </Text>
          </View>
        )}

        <View style={styles.headerInfo}>
          <Text style={styles.providerName} numberOfLines={1}>{item.providerName}</Text>
          {item.city ? (
            <Text style={styles.location} numberOfLines={1}>
              📍 {[item.neighborhood, item.city].filter(Boolean).join(', ')}
            </Text>
          ) : null}
        </View>

        {/* Avaliação */}
        <View style={styles.ratingContainer}>
          <Text style={styles.stars}>{renderStars(item.avgRating)}</Text>
          <Text style={styles.ratingText}>
            {item.avgRating.toFixed(1)}
            {item.totalReviews > 0 ? ` (${item.totalReviews})` : ''}
          </Text>
        </View>
      </View>

      {/* Linha divisória */}
      <View style={styles.divider} />

      {/* Serviço */}
      <View style={styles.serviceBody}>
        {/* Categoria */}
        <View style={styles.categoryRow}>
          {item.categoryIcon ? <Text style={styles.categoryIcon}>{item.categoryIcon}</Text> : null}
          <Text style={styles.categoryName}>{item.categoryName}</Text>
        </View>

        {/* Título */}
        <Text style={styles.serviceTitle} numberOfLines={2}>{item.title}</Text>

        {/* Descrição */}
        {item.description ? (
          <Text style={styles.serviceDescription} numberOfLines={2}>
            {item.description}
          </Text>
        ) : null}
      </View>

      {/* Footer: preço + duração */}
      <View style={styles.footer}>
        <Text style={styles.price}>{formatPrice(item.basePrice)}</Text>
        {item.durationMinutes ? (
          <Text style={styles.duration}>
            ⏱ {item.durationMinutes >= 60
              ? `${Math.floor(item.durationMinutes / 60)}h${item.durationMinutes % 60 > 0 ? item.durationMinutes % 60 + 'min' : ''}`
              : `${item.durationMinutes}min`}
          </Text>
        ) : null}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 14,
    marginHorizontal: 16,
    marginVertical: 6,
    padding: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 2,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    marginRight: 10,
  },
  avatarPlaceholder: {
    backgroundColor: '#111',
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarInitial: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  headerInfo: {
    flex: 1,
  },
  providerName: {
    fontSize: 14,
    fontWeight: '700',
    color: '#111',
  },
  location: {
    fontSize: 12,
    color: '#888',
    marginTop: 2,
  },
  ratingContainer: {
    alignItems: 'flex-end',
  },
  stars: {
    fontSize: 11,
    color: '#f5a623',
    letterSpacing: 1,
  },
  ratingText: {
    fontSize: 11,
    color: '#888',
    marginTop: 2,
  },
  divider: {
    height: 1,
    backgroundColor: '#f0f0f0',
    marginVertical: 10,
  },
  serviceBody: {
    marginBottom: 10,
  },
  categoryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  categoryIcon: {
    fontSize: 14,
    marginRight: 4,
  },
  categoryName: {
    fontSize: 12,
    color: '#666',
    fontWeight: '500',
  },
  serviceTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111',
    marginBottom: 4,
  },
  serviceDescription: {
    fontSize: 13,
    color: '#666',
    lineHeight: 18,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  price: {
    fontSize: 16,
    fontWeight: '800',
    color: '#000',
  },
  duration: {
    fontSize: 13,
    color: '#888',
  },
});
