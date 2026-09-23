export type Unit = 'G' | 'ML' | 'PIECE';
export type DateType = 'EXPIRATION' | 'BEST_BEFORE';
export type DateStatus = 'UPCOMING' | 'PRIORITY' | 'DATE_PASSED';
export type MovementType = 'CONSUMED' | 'WASTED';
export type SuggestionSource = 'CATALOG' | 'AI';

export interface User {
  id: string;
  name: string;
}

export interface Ingredient {
  id: string;
  name: string;
}

export interface FoodItem {
  id: string;
  ingredientId: string;
  name: string;
  remainingQuantity: number;
  unit: Unit;
  labelDate: string;
  dateType: DateType;
  dateStatus?: DateStatus;
  daysRemaining?: number;
  version: number;
  archived: boolean;
  userId?: string;
  createdAt?: string;
}

export interface FoodCreateDTO {
  userId?: string;
  ingredientId: string;
  name: string;
  quantity: number;
  labelDate: string;
  dateType: DateType;
}

export interface FoodUpdateDTO {
  name: string;
  labelDate: string;
  dateType: DateType;
  version: number;
}

export interface MovementCreateDTO {
  operationId: string;
  type: MovementType;
  quantity: number;
  expectedVersion: number;
}

export interface MovementResponseDTO {
  movementId: string;
  foodId: string;
  newRemainingQuantity: number;
  newVersion: number;
  wasPriorityAtConsumption: boolean;
}

export interface SuggestionIngredient {
  ingredientId: string;
  name: string;
  requiredQuantity: number;
  availableQuantity: number;
  missingQuantity: number;
  unit: Unit;
}

export interface RecipeSuggestion {
  recipeId: string;
  title: string;
  servings: number;
  instructions: string | string[];
  source: SuggestionSource;
  ingredients: SuggestionIngredient[];
  priorityFoodIds: string[];
}

export interface ImpactSummary {
  consumedByUnit: Record<Unit, number>;
  wastedByUnit: Record<Unit, number>;
}

export interface ApiError {
  code: string;
  message: string;
  details?: any;
}

// === MARKETPLACE & RESERVATIONS ===

export type ListingType = 'SALE' | 'DONATION';
export type ListingStatus = 'ACTIVE' | 'RESERVED' | 'CLOSED';

export interface ListingResponse {
  id: string;
  foodId: string;
  ownerId: string;
  ownerName: string;
  foodName: string;
  ingredientName: string;
  type: ListingType;
  status: ListingStatus;
  price: number | null;
  description: string;
  labelDate: string;
  latitude?: number;
  longitude?: number;
  locationName?: string;
  createdAt?: string;
}

export interface ListingCreateDTO {
  foodId: string;
  type: ListingType;
  price?: number | null;
  description: string;
  latitude?: number;
  longitude?: number;
  locationName?: string;
}

export type ReservationStatus = 'ACTIVE' | 'CANCELLED' | 'COMPLETED';

export interface ReservationResponse {
  id: string;
  listingId: string;
  userId: string;
  userName: string;
  ownerName: string;
  foodName: string;
  status: ReservationStatus;
  pickupCode: string; // 4 dígitos como String
  createdAt: string;
}

export interface ReservationCreateDTO {
  listingId: string;
  userId: string;
}

export interface DashboardMetrics {
  foodsInInventory: number;
  activeListings: number;
  activeReservations: number;
  completedExchanges: number;
  completedDonations: number;
  completedSales: number;
}

export type UrgencyLevel = 'URGENT' | 'HIGH' | 'MEDIUM';

export interface ExpiringFoodResponse {
  foodId: string;
  name: string;
  labelDate: string;
  daysRemaining: number;
  urgency: UrgencyLevel;
  recommendation: string;
}
