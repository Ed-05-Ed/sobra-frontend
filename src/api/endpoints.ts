import { apiFetch } from './client';
import {
  User,
  Ingredient,
  FoodItem,
  FoodCreateDTO,
  FoodUpdateDTO,
  MovementCreateDTO,
  MovementResponseDTO,
  RecipeSuggestion,
  ImpactSummary,
  ListingResponse,
  ListingCreateDTO,
  ReservationResponse,
  ReservationCreateDTO,
  DashboardMetrics,
  ExpiringFoodResponse,
} from '../types/api';

export const api = {

  // Dashboard
  
  // Alimentos próximos a vencer con recomendación
  getExpiringFoods: (userId: string, days: number = 5) =>
    apiFetch<ExpiringFoodResponse[]>(`/api/foods/expiring?userId=${userId}&days=${days}`),

  getDashboard: (userId: string) =>
    apiFetch<DashboardMetrics>(`/api/dashboard?userId=${userId}`),

  // Reservas recibidas (Mis entregas pendientes)
  getReceivedReservations: (ownerId: string) =>
    apiFetch<ReservationResponse[]>(`/api/reservations/received?ownerId=${ownerId}`),

  // Comprobar salud
  getHealth: () => apiFetch<{ status: string }>('/actuator/health'),

  // Usuarios demo
  getUsers: () => apiFetch<User[]>('/api/users'),

  // Catálogo de ingredientes
  getIngredients: () => apiFetch<Ingredient[]>('/api/ingredients'),

  // Inventario
  getFoods: (userId?: string) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch<FoodItem[]>(`/api/foods${query}`);
  },

  getFoodById: (id: string) => apiFetch<FoodItem>(`/api/foods/${id}`),

  createFood: (data: FoodCreateDTO) =>
    apiFetch<FoodItem>('/api/foods', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateFood: (id: string, data: FoodUpdateDTO) =>
    apiFetch<FoodItem>(`/api/foods/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  archiveFood: (id: string, version: number) =>
    apiFetch<void>(`/api/foods/${id}?version=${version}`, {
      method: 'DELETE',
    }),

  // Movimientos
  registerMovement: (foodId: string, data: MovementCreateDTO) =>
    apiFetch<MovementResponseDTO>(`/api/foods/${foodId}/movements`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  // Recetas
  getSuggestions: () => apiFetch<RecipeSuggestion[]>('/api/suggestions'),

  // Impacto
  getImpact: () => apiFetch<ImpactSummary>('/api/impact'),

  // === MARKETPLACE ===
  getListings: (userId?: string) => {
    const query = userId ? `?userId=${userId}` : '';
    return apiFetch<ListingResponse[]>(`/api/listings${query}`);
  },

  createListing: (data: ListingCreateDTO) =>
    apiFetch<ListingResponse>('/api/listings', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  closeListing: (listingId: string) =>
    apiFetch<void>(`/api/listings/${listingId}/close`, {
      method: 'PATCH',
    }),

  // === RESERVACIONES ===
  getReservations: (userId: string) =>
    apiFetch<ReservationResponse[]>(`/api/reservations?userId=${userId}`),

  createReservation: (data: ReservationCreateDTO) =>
    apiFetch<ReservationResponse>('/api/reservations', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  cancelReservation: (reservationId: string) =>
    apiFetch<void>(`/api/reservations/${reservationId}/cancel`, {
      method: 'PATCH',
    }),

  completeReservation: (reservationId: string, pickupCode: string) =>
    apiFetch<void>(`/api/reservations/${reservationId}/complete`, {
      method: 'PATCH',
      body: JSON.stringify({ pickupCode }),
    }),
};
