import React, { useState, useEffect } from 'react';
import { FoodItem, User, Unit } from '../types/api';
import { api } from '../api/endpoints';
import { StatusBadge } from '../components/StatusBadge';
import { MovementModal } from '../components/MovementModal';
import { PublishModal } from '../components/PublishModal';
import { SearchIcon, PlusIcon, EditIcon, TrashIcon } from '../components/Icons';

interface Props {
  currentUser: User;
  onNavigate: (view: any, data?: any) => void;
}

const formatUnitLabel = (unit: Unit | string) => {
  switch (unit) {
    case 'PIECE':
      return 'pz';
    case 'G':
      return 'g';
    case 'ML':
      return 'ml';
    default:
      return unit;
  }
};

export const InventoryPage: React.FC<Props> = ({ currentUser, onNavigate }) => {
  const [foods, setFoods] = useState<FoodItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedFoodForUse, setSelectedFoodForUse] = useState<FoodItem | null>(null);
  const [selectedFoodForPublish, setSelectedFoodForPublish] = useState<FoodItem | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await api.getFoods(currentUser.id);
      setFoods(data.filter((f) => !f.archived));
    } catch (err: any) {
      setError(err.message || 'Error al obtener inventario.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentUser.id]);

  const handleArchive = async (item: FoodItem) => {
    if (!window.confirm(`¿Confirmas retirar "${item.name}" del inventario? (No se computará como aprovechamiento)`)) {
      return;
    }
    setActionLoadingId(item.id);
    try {
      await api.archiveFood(item.id, item.version);
      loadData();
    } catch (err: any) {
      alert(err.message || 'Error al archivar el lote.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const filteredFoods = foods
    .filter((f) => f.name.toLowerCase().includes(searchTerm.toLowerCase()))
    .sort((a, b) => new Date(a.labelDate).getTime() - new Date(b.labelDate).getTime());

  return (
    <div className="view-wrapper">
      <header className="page-nav-header">
        <div>
          <span className="hero-tag">DESPENSA DE {currentUser.name.toUpperCase()}</span>
          <h1 className="hero-headline">Inventario ({filteredFoods.length})</h1>
        </div>
        <button className="btn-add-circle" onClick={() => onNavigate('create-food')} title="Nuevo alimento">
          <PlusIcon size={18} />
        </button>
      </header>

      <div className="search-pill-container">
        <SearchIcon size={16} className="search-icon-decor" />
        <input
          type="search"
          placeholder="Buscar ingrediente o producto..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        {searchTerm && (
          <button className="btn-clear-search" onClick={() => setSearchTerm('')}>✕</button>
        )}
      </div>

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
      ) : filteredFoods.length === 0 ? (
        <div className="clean-empty-box">
          <h4>{searchTerm ? 'Sin coincidencias' : 'Despensa vacía'}</h4>
          <p>{searchTerm ? 'Prueba con otro término de búsqueda.' : `${currentUser.name} no tiene alimentos registrados todavía.`}</p>
          {!searchTerm && (
            <button className="btn-primary-sm" onClick={() => onNavigate('create-food')}>
              Añadir alimento
            </button>
          )}
        </div>
      ) : (
        <div className="inventory-card-grid">
          {filteredFoods.map((item) => (
            <article key={item.id} className="inventory-card">
              <div className="card-top-row">
                <div className="title-and-date">
                  <h3 className="card-product-title">{item.name}</h3>
                  <span className="card-date-meta">Etiqueta: {item.labelDate}</span>
                </div>
                <div className="card-qty-badge">
                  <span className="qty-val">{item.remainingQuantity}</span>
                  <span className="qty-unit">{formatUnitLabel(item.unit)}</span>
                </div>
              </div>

              <div className="card-mid-row">
                <StatusBadge
                  status={item.dateStatus}
                  dateType={item.dateType}
                  daysRemaining={item.daysRemaining}
                  labelDate={item.labelDate}
                />
              </div>

              <div className="card-bottom-actions">
                <button
                  className="btn-consume-prominent"
                  onClick={() => setSelectedFoodForUse(item)}
                  disabled={item.remainingQuantity <= 0}
                >
                  Registrar uso
                </button>

                <button
                  className="btn-publish-outline"
                  onClick={() => setSelectedFoodForPublish(item)}
                  title="Ofrecer en donación o venta en el Marketplace"
                >
                  Publicar
                </button>

                <div className="utility-buttons">
                  <button
                    className="btn-util"
                    title="Editar metadatos"
                    onClick={() => onNavigate('create-food', item)}
                  >
                    <EditIcon size={14} />
                  </button>
                  <button
                    className="btn-util danger"
                    title="Retirar lote"
                    onClick={() => handleArchive(item)}
                    disabled={actionLoadingId === item.id}
                  >
                    <TrashIcon size={14} />
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}

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
            alert('¡Alimento publicado en la comunidad con éxito!');
            onNavigate('marketplace');
          }}
        />
      )}
    </div>
  );
};