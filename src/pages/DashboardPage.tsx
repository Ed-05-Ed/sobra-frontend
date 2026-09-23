import React, { useState, useEffect } from 'react';
import { FoodItem, User, DashboardMetrics, ExpiringFoodResponse } from '../types/api';
import { api } from '../api/endpoints';
import { StatusBadge } from '../components/StatusBadge';
import { MovementModal } from '../components/MovementModal';
import { PublishModal } from '../components/PublishModal';
import { SparklesIcon, PlusIcon, AlertCircleIcon, ClockIcon } from '../components/Icons';

interface Props {
  currentUser: User;
  onNavigate: (view: any) => void;
}

export const DashboardPage: React.FC<Props> = ({ currentUser, onNavigate }) => {
  const [foods, setFoods] = useState<FoodItem[]>([]);
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [expiringFoods, setExpiringFoods] = useState<ExpiringFoodResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedFoodForUse, setSelectedFoodForUse] = useState<FoodItem | null>(null);
  const [selectedFoodForPublish, setSelectedFoodForPublish] = useState<any | null>(null);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [foodsData, dashboardData, expiringData] = await Promise.all([
        api.getFoods(currentUser.id),
        api.getDashboard(currentUser.id).catch(() => null),
        api.getExpiringFoods(currentUser.id, 5).catch(() => []),
      ]);
      setFoods(foodsData.filter((f) => !f.archived && f.remainingQuantity > 0));
      setMetrics(dashboardData);
      setExpiringFoods(expiringData);
    } catch (err: any) {
      setError(err.message || 'Error al conectar con la despensa.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentUser.id]);

  const formatDaysText = (days: number) => {
    if (days <= 0) return 'Vence hoy';
    if (days === 1) return 'Vence mañana';
    return `Vence en ${days} días`;
  };

  const getUrgencyBadge = (urgency: string) => {
    switch (urgency) {
      case 'URGENT':
        return { label: 'URGENTE', className: 'urgency-badge-urgent' };
      case 'HIGH':
        return { label: 'ALTA PRIORIDAD', className: 'urgency-badge-high' };
      case 'MEDIUM':
      default:
        return { label: 'MODERADA', className: 'urgency-badge-medium' };
    }
  };

  return (
    <div className="view-wrapper">
      <section className="dashboard-hero">
        <div className="hero-titles">
          <span className="hero-tag">PANEL DE {currentUser.name.toUpperCase()}</span>
          <h1 className="hero-headline">Resumen General</h1>
        </div>
        <button className="btn-icon-accent" onClick={loadData} title="Sincronizar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67" />
          </svg>
        </button>
      </section>

      {error && (
        <div className="alert-banner error" role="alert">
          <AlertCircleIcon size={16} />
          <div className="alert-content">
            <span>{error}</span>
            <button className="btn-link" onClick={loadData}>Reintentar</button>
          </div>
        </div>
      )}

      {/* Métricas Principales del Backend */}
      <section className="bento-grid">
        <div className="bento-card sub-metric" onClick={() => onNavigate('inventory')} style={{ cursor: 'pointer' }}>
          <span className="bento-label">EN DESPENSA</span>
          <div className="bento-number">{metrics ? metrics.foodsInInventory : foods.length}</div>
          <p className="bento-caption">Alimentos activos</p>
        </div>

        <div className="bento-card sub-metric" onClick={() => onNavigate('marketplace')} style={{ cursor: 'pointer' }}>
          <span className="bento-label">PUBLICACIONES</span>
          <div className="bento-number">{metrics?.activeListings ?? 0}</div>
          <p className="bento-caption">En el Marketplace</p>
        </div>

        <div className="bento-card sub-metric" onClick={() => onNavigate('marketplace')} style={{ cursor: 'pointer' }}>
          <span className="bento-label">RESERVAS ACTIVAS</span>
          <div className="bento-number">{metrics?.activeReservations ?? 0}</div>
          <p className="bento-caption">Por recoger</p>
        </div>

        <div className="bento-card sub-metric">
          <span className="bento-label">INTERCAMBIOS</span>
          <div className="bento-number" style={{ color: 'var(--brand-deep)' }}>
            {metrics?.completedExchanges ?? 0}
          </div>
          <p className="bento-caption">
            {metrics ? `${metrics.completedDonations} donados / ${metrics.completedSales} ventas` : 'Completados'}
          </p>
        </div>
      </section>

      {/* Acciones Rápidas */}
      <section className="action-row">
        <button className="action-pill-card primary" onClick={() => onNavigate('create-food')}>
          <div className="action-icon-wrap"><PlusIcon size={18} /></div>
          <div>
            <strong>Añadir alimento</strong>
            <span>Registra lote con fecha</span>
          </div>
        </button>

        <button className="action-pill-card secondary" onClick={() => onNavigate('marketplace')}>
          <div className="action-icon-wrap recipe-accent"><span></span></div>
          <div>
            <strong>Comunidad</strong>
            <span>Marketplace y Entregas</span>
          </div>
        </button>
      </section>

      {/* SECCIÓN INTELIGENTE: PRODUCTOS PRÓXIMOS A VENCER */}
      <section className="content-block">
        <div className="block-header">
          <div>
            <h2 className="block-title">Detección de Vencimiento</h2>
            <p className="block-subtitle">Recomendaciones proactivas para evitar desperdicio</p>
          </div>
          <span className="badge-tag-count">{expiringFoods.length} próximos</span>
        </div>

        {isLoading ? (
          <div className="skeleton-list">
            <div className="skeleton-card" />
            <div className="skeleton-card" />
          </div>
        ) : expiringFoods.length === 0 ? (
          <div className="clean-empty-box">
            <div className="clean-empty-icon"><SparklesIcon size={24} /></div>
            <h4>Sin alimentos en riesgo cercano</h4>
            <p>No tienes productos por vencer en los próximos 5 días.</p>
          </div>
        ) : (
          <div className="expiring-cards-stack">
            {expiringFoods.map((item) => {
              const badge = getUrgencyBadge(item.urgency);
              const daysLabel = formatDaysText(item.daysRemaining);

              return (
                <article key={item.foodId} className={`expiring-card ${item.urgency.toLowerCase()}-accent`}>
                  <div className="expiring-top-row">
                    <div>
                      <h3 className="expiring-product-name">{item.name}</h3>
                      <div className="expiring-date-meta">
                        <ClockIcon size={13} />
                        <span>{daysLabel} ({item.labelDate})</span>
                      </div>
                    </div>
                    <span className={`urgency-pill ${badge.className}`}>
                      {badge.label}
                    </span>
                  </div>

                  {/* Recomendación textual generada por el backend */}
                  <div className="recommendation-callout">
                    <span className="rec-bulb"></span>
                    <p className="rec-text">{item.recommendation}</p>
                  </div>

                  <div className="expiring-actions-row">
                    <button
                      className="btn-publish-direct"
                      onClick={() =>
                        setSelectedFoodForPublish({
                          id: item.foodId,
                          name: item.name,
                        })
                      }
                    >
                      Publicar en Marketplace
                    </button>
                    <button
                      className="btn-consume-direct"
                      onClick={() => {
                        const found = foods.find((f) => f.id === item.foodId);
                        if (found) {
                          setSelectedFoodForUse(found);
                        } else {
                          onNavigate('inventory');
                        }
                      }}
                    >
                      Registrar Uso
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>

      {selectedFoodForUse && (
        <MovementModal
          food={selectedFoodForUse}
          onClose={() => setSelectedFoodForUse(null)}
          onSuccess={() => {
            setSelectedFoodForUse(null);
            loadData();
          }}
        />
      )}

      {selectedFoodForPublish && (
        <PublishModal
          food={selectedFoodForPublish}
          onClose={() => setSelectedFoodForPublish(null)}
          onSuccess={() => {
            setSelectedFoodForPublish(null);
            alert('¡Publicación creada exitosamente en el Marketplace!');
            onNavigate('marketplace');
          }}
        />
      )}
    </div>
  );
};
