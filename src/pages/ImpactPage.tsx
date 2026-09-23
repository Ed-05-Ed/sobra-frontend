import React, { useState, useEffect } from 'react';
import { ImpactSummary, Unit } from '../types/api';
import { api } from '../api/endpoints';
import { ChartIcon, SparklesIcon } from '../components/Icons';

export const ImpactPage: React.FC = () => {
  const [impact, setImpact] = useState<ImpactSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await api.getImpact();
      setImpact(data);
    } catch (err: any) {
      setError(err.message || 'Error al obtener indicadores.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const units: Unit[] = ['G', 'ML', 'PIECE'];
  const unitLabels: Record<Unit, { title: string; symbol: string }> = {
    G: { title: 'Masa sólida', symbol: 'g' },
    ML: { title: 'Líquidos', symbol: 'ml' },
    PIECE: { title: 'Unidades enteras', symbol: 'pz' },
  };

  return (
    <div className="view-wrapper">
      <header className="page-nav-header">
        <div>
          <span className="hero-tag">BALANCE HISTÓRICO</span>
          <h1 className="hero-headline">Impacto y Eficiencia</h1>
        </div>
        <button className="btn-icon-accent" onClick={loadData} title="Actualizar">
          <ChartIcon size={16} />
        </button>
      </header>

      {error && (
        <div className="alert-banner error" role="alert">
          <span>{error}</span>
          <button className="btn-link" onClick={loadData}>Reintentar</button>
        </div>
      )}

      {isLoading ? (
        <div className="skeleton-list">
          <div className="skeleton-card" />
          <div className="skeleton-card" />
        </div>
      ) : !impact ? (
        <div className="clean-empty-box">
          <p>Aún no se registran movimientos en el sistema.</p>
        </div>
      ) : (
        <div className="impact-overview">
          <div className="impact-cards-column">
            {units.map((u) => {
              const consumed = impact.consumedByUnit?.[u] || 0;
              const wasted = impact.wastedByUnit?.[u] || 0;
              const total = consumed + wasted;
              const rate = total > 0 ? Math.round((consumed / total) * 100) : 0;

              return (
                <article key={u} className="impact-kpi-card">
                  <div className="kpi-card-header">
                    <div>
                      <span className="kpi-metric-title">{unitLabels[u].title}</span>
                      <h3 className="kpi-efficiency-number">{rate}% aprovechado</h3>
                    </div>
                    <span className="kpi-symbol-pill">{unitLabels[u].symbol}</span>
                  </div>

                  <div className="kpi-bars-wrapper">
                    <div className="kpi-meter-bg">
                      <div className="kpi-meter-fill" style={{ width: `${rate}%` }} />
                    </div>
                  </div>

                  <div className="kpi-details-grid">
                    <div className="kpi-detail-item success">
                      <span className="detail-tag">Consumido a tiempo</span>
                      <strong className="detail-val">{consumed.toLocaleString()} <small>{unitLabels[u].symbol}</small></strong>
                    </div>
                    <div className="kpi-detail-item warning">
                      <span className="detail-tag">Desperdicio</span>
                      <strong className="detail-val">{wasted.toLocaleString()} <small>{unitLabels[u].symbol}</small></strong>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
