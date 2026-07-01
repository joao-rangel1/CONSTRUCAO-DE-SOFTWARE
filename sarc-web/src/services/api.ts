import { getToken } from '../auth/keycloak';
import type {
  Allocation,
  AllocationRequest,
  ApiError,
  ProfessorFilter,
  Resource,
  ResourceFilter,
  ResourceRequest,
  ScheduleItem
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

type QueryValue = string | number | undefined | null;

function buildQuery(params: Record<string, QueryValue>) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      query.set(key, String(value));
    }
  });

  const queryString = query.toString();
  return queryString ? `?${queryString}` : '';
}

async function request<T>(path: string, options: RequestInit = {}) {
  const token = await getToken();
  const headers = new Headers(options.headers);

  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    let message = `Falha na requisicao (${response.status})`;

    try {
      const error = (await response.json()) as ApiError;
      message = error.message ?? error.error ?? message;
      if (error.fieldErrors) {
        message = `${message}: ${Object.values(error.fieldErrors).join(', ')}`;
      }
    } catch {
      // Keeps the default message when the response is not JSON.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  schedules: {
    list(filters: { data?: string; professor?: string; disciplina?: string; recurso?: string }) {
      return request<ScheduleItem[]>(`/api/schedules${buildQuery(filters)}`);
    },
    resources() {
      return request<ResourceFilter[]>('/api/schedules/resources');
    },
    professors() {
      return request<ProfessorFilter[]>('/api/schedules/professors');
    }
  },
  allocations: {
    mine() {
      return request<Allocation[]>('/api/allocations/my');
    },
    create(data: AllocationRequest) {
      return request<Allocation>('/api/allocations', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },
    update(id: number, data: AllocationRequest) {
      return request<Allocation>(`/api/allocations/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },
    remove(id: number) {
      return request<void>(`/api/allocations/${id}`, { method: 'DELETE' });
    }
  },
  resources: {
    async list() {
      const page = await request<{ content: Resource[] }>('/api/resources?size=100');
      return page.content;
    },
    publicList() {
      return request<ResourceFilter[]>('/api/resources/public');
    },
    create(data: ResourceRequest) {
      return request<Resource>('/api/resources', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },
    update(id: number, data: ResourceRequest) {
      return request<Resource>(`/api/resources/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },
    activate(id: number) {
      return request<Resource>(`/api/resources/${id}/activate`, { method: 'PATCH' });
    },
    deactivate(id: number) {
      return request<Resource>(`/api/resources/${id}/deactivate`, { method: 'PATCH' });
    }
  }
};
