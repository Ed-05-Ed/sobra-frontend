import { ApiError } from '../types/api';

const RAW_URL = import.meta.env.VITE_API_BASE_URL || '/api';
const BASE_URL = RAW_URL.endsWith('/') ? RAW_URL.slice(0, -1) : RAW_URL;

export class CustomApiError extends Error {
  status: number;
  code: string;
  details?: Record<string, string>;

  constructor(status: number, errorData: ApiError) {
    super(errorData.message || 'Error en la solicitud');
    this.name = 'CustomApiError';
    this.status = status;
    this.code = errorData.code || 'UNKNOWN_ERROR';
    this.details = errorData.details;
  }
}

export async function apiFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${endpoint}`;

  try {
    const res = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!res.ok) {
      let errorData: ApiError;
      try {
        errorData = await res.json();
        console.error('Respuesta de error del backend:', res.status, errorData);
      } catch {
        errorData = {
          code: `HTTP_${res.status}`,
          message: res.statusText || 'Error inesperado del servidor',
        };
      }
      throw new CustomApiError(res.status, errorData);
    }

    if (res.status === 204) {
      return {} as T;
    }

    return res.json();
  } catch (err) {
    if (err instanceof CustomApiError) {
      throw err;
    }
    throw new CustomApiError(0, {
      code: 'NETWORK_ERROR',
      message: 'No se pudo conectar con el backend. Verifica que el túnel de Cloudflare esté activo.',
    });
  }
}
