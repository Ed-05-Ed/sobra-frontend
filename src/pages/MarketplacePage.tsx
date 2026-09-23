import React, { useState, useEffect } from 'react';
import { User, ListingResponse, ReservationResponse, ListingType } from '../types/api';
import { api } from '../api/endpoints';
import { SparklesIcon, CheckCircleIcon, AlertCircleIcon } from '../components/Icons';

interface Props {
  currentUser: User;
  onNavigate: (view: any) => void;
}

export const MarketplacePage: React.FC<Props> = ({ currentUser, onNavigate }) => {
  const [subTab, setSubTab] = useState<'explore' | 'my-deliveries' | 'my-reservations' | 'my-listings'>('explore');
  const [listings, setListings] = useState<ListingResponse[]>([]);
  const [myListings, setMyListings] = useState<ListingResponse[]>([]);
  const [receivedReservations, setReceivedReservations] = useState<ReservationResponse[]>([]);
  const [myReservations, setMyReservations] = useState<ReservationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [filterType, setFilterType] = useState<'ALL' | ListingType>('ALL');
  const [newReservation, setNewReservation] = useState<ReservationResponse | null>(null);

  // Código de 4 dígitos para cada entrega
  const [pickupCodeInput, setPickupCodeInput] = useState<{ [resId: string]: string }>({});
  const [completingId, setCompletingId] = useState<string | null>(null);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      if (subTab === 'explore') {
        const data = await api.getListings();
        setListings(data);
      } else if (subTab === 'my-deliveries') {
        const data = await api.getReceivedReservations(currentUser.id);
        setReceivedReservations(data);
      } else if (subTab === 'my-reservations') {
        const data = await api.getReservations(currentUser.id);
        setMyReservations(data);
      } else if (subTab === 'my-listings') {
        const data = await api.getListings(currentUser.id);
        setMyListings(data);
      }
    } catch (err: any) {
      setError(err.message || 'Error al sincronizar con el servidor.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [subTab, currentUser.id]);

  const handleReserve = async (listing: ListingResponse) => {
    if (listing.ownerId === currentUser.id) {
      alert('No puedes reservar tu propia publicación.');
      return;
    }
    try {
      const res = await api.createReservation({
        listingId: listing.id,
        userId: currentUser.id,
      });
      setNewReservation(res);
      loadData();
    } catch (err: any) {
      alert(err.message || 'La publicación ya no está disponible.');
      loadData();
    }
  };

  const handleCancelReservation = async (reservationId: string) => {
    if (!window.confirm('¿Cancelar esta reserva? El alimento volverá a estar disponible en el Marketplace.')) {
      return;
    }
    try {
      await api.cancelReservation(reservationId);
      loadData();
    } catch (err: any) {
      alert(err.message || 'No se pudo cancelar la reserva.');
    }
  };

  const handleCompleteDelivery = async (reservation: ReservationResponse) => {
    const code = pickupCodeInput[reservation.id] || '';
    if (code.length !== 4) {
      alert('Por favor introduce exactamente los 4 dígitos que te dé la persona que recoge.');
      return;
    }

    setCompletingId(reservation.id);
    try {
      await api.completeReservation(reservation.id, code);
      alert('✓ ¡Entrega completada exitosamente!');
      loadData();
    } catch (err: any) {
      alert(err.message || 'El código de recogida es incorrecto.');
    } finally {
      setCompletingId(null);
    }
  };

  const filteredExplore = listings.filter((l) => {
    if (filterType === 'ALL') return true;
    return l.type === filterType;
  });

  return (
    <div className="view-wrapper">
      <header className="page-nav-header">
        <div>
          <span className="hero-tag">COMUNIDAD Y MARKETPLACE</span>
          <h1 className="hero-headline">Intercambio Solidario</h1>
        </div>
        <button className="btn-icon-accent" onClick={loadData} title="Refrescar">
          
        </button>
      </header>

      {/* Sub-tabs de 4 opciones */}
      <div className="segmented-selector four-cols">
        <button
          className={`segmented-option ${subTab === 'explore' ? 'selected' : ''}`}
          onClick={() => setSubTab('explore')}
        >
          Explorar
        </button>
        <button
          className={`segmented-option ${subTab === 'my-deliveries' ? 'selected' : ''}`}
          onClick={() => setSubTab('my-deliveries')}
        >
          Mis Entregas
        </button>
        <button
          className={`segmented-option ${subTab === 'my-reservations' ? 'selected' : ''}`}
          onClick={() => setSubTab('my-reservations')}
        >
          Mis Reservas
        </button>
        <button
          className={`segmented-option ${subTab === 'my-listings' ? 'selected' : ''}`}
          onClick={() => setSubTab('my-listings')}
        >
          Mis Publicaciones
        </button>
      </div>

      {error && (
        <div className="alert-banner error" role="alert">
          <span>{error}</span>
          <button className="btn-link" onClick={loadData}>Reintentar</button>
        </div>
      )}

      {/* 1. EXPLORAR */}
      {subTab === 'explore' && (
        <>
          <div className="filter-chips-row">
            <button
              className={`filter-chip ${filterType === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilterType('ALL')}
            >
              Todos ({listings.length})
            </button>
            <button
              className={`filter-chip ${filterType === 'DONATION' ? 'active' : ''}`}
              onClick={() => setFilterType('DONATION')}
            >
              Donaciones
            </button>
            <button
              className={`filter-chip ${filterType === 'SALE' ? 'active' : ''}`}
              onClick={() => setFilterType('SALE')}
            >
              Venta económica
            </button>
          </div>

          {isLoading ? (
            <div className="skeleton-list">
              <div className="skeleton-card" />
              <div className="skeleton-card" />
            </div>
          ) : filteredExplore.length === 0 ? (
            <div className="clean-empty-box">
              <h4>Sin publicaciones disponibles</h4>
              <p>Puedes compartir un alimento desde tu despensa para comenzar.</p>
              <button className="btn-primary-sm" onClick={() => onNavigate('inventory')}>
                Ir a mi despensa para publicar
              </button>
            </div>
          ) : (
            <div className="inventory-card-grid">
              {filteredExplore.map((item) => {
                const isMine = item.ownerId === currentUser.id;
                return (
                  <article key={item.id} className="inventory-card">
                    <div className="card-top-row">
                      <div>
                        <h3 className="card-product-title">{item.foodName}</h3>
                        <span className="card-date-meta">Ofrecido por <strong>{item.ownerName}</strong></span>
                      </div>
                      <span className={`listing-type-badge ${item.type === 'DONATION' ? 'donation' : 'sale'}`}>
                        {item.type === 'DONATION' ? 'DONACIÓN' : `$${item.price?.toFixed(2)} MXN`}
                      </span>
                    </div>

                    <p className="listing-description">{item.description}</p>

                    <div className="listing-location-row">
                      <span>{item.locationName || 'Santiago Tianguistenco'}</span>
                      {item.latitude && item.longitude && (
                        <a
                          href={`https://www.google.com/maps?q=${item.latitude},${item.longitude}`}
                          target="_blank"
                          rel="noreferrer"
                          className="btn-map-link"
                        >
                          Ver mapa
                        </a>
                      )}
                    </div>

                    <div className="card-bottom-actions">
                      {isMine ? (
                        <span className="text-muted text-sm font-semibold">Es tu publicación</span>
                      ) : (
                        <button
                          className="btn-consume-prominent"
                          onClick={() => handleReserve(item)}
                        >
                          Reservar para recoger
                        </button>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* 2. MIS ENTREGAS (RESERVAS RECIBIDAS DE OTROS) */}
      {subTab === 'my-deliveries' && (
        <>
          <p className="section-hint mb-2">
            Alimentos de tus publicaciones que otros usuarios han reservado. Introduce su código al entregárselos:
          </p>

          {isLoading ? (
            <div className="skeleton-list">
              <div className="skeleton-card" />
            </div>
          ) : receivedReservations.length === 0 ? (
            <div className="clean-empty-box">
              <h4>Sin entregas pendientes</h4>
              <p>Nadie ha reservado tus publicaciones todavía.</p>
            </div>
          ) : (
            <div className="inventory-card-grid">
              {receivedReservations.map((res) => (
                <article key={res.id} className="inventory-card">
                  <div className="card-top-row">
                    <div>
                      <h3 className="card-product-title">{res.foodName}</h3>
                      <span className="card-date-meta">Reservado por: <strong>{res.userName}</strong></span>
                    </div>
                    <span className={`listing-status-tag ${res.status.toLowerCase()}`}>
                      {res.status === 'ACTIVE' ? 'POR ENTREGAR' : res.status === 'COMPLETED' ? 'ENTREGADA' : 'CANCELADA'}
                    </span>
                  </div>

                  {res.status === 'ACTIVE' && (
                    <div className="delivery-verification-box">
                      <div className="verification-header">
                        <strong>Pídele a {res.userName} su código de 4 dígitos:</strong>
                      </div>
                      <div className="code-input-row">
                        <input
                          type="text"
                          maxLength={4}
                          placeholder="_ _ _ _"
                          value={pickupCodeInput[res.id] || ''}
                          onChange={(e) =>
                            setPickupCodeInput({
                              ...pickupCodeInput,
                              [res.id]: e.target.value.replace(/[^0-9]/g, ''),
                            })
                          }
                          className="pickup-code-input"
                        />
                        <button
                          className="btn-primary-sm"
                          onClick={() => handleCompleteDelivery(res)}
                          disabled={completingId === res.id}
                        >
                          {completingId === res.id ? 'Comprobando...' : 'Confirmar Entrega'}
                        </button>
                      </div>
                    </div>
                  )}

                  {res.status === 'COMPLETED' && (
                    <div className="alert-banner success mt-2">
                      <CheckCircleIcon size={16} />
                      <span>Entrega confirmada y completada.</span>
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </>
      )}

      {/* 3. MIS RESERVAS (LO QUE YO RESERVÉ) */}
      {subTab === 'my-reservations' && (
        <>
          <p className="section-hint mb-2">
            Productos que reservaste. Muéstrale este código al dueño al momento de recogerlo:
          </p>

          {isLoading ? (
            <div className="skeleton-list">
              <div className="skeleton-card" />
            </div>
          ) : myReservations.length === 0 ? (
            <div className="clean-empty-box">
              <h4>No tienes reservas activas</h4>
              <p>Explora el Marketplace para reservar alimentos de otros vecinos.</p>
              <button className="btn-primary-sm" onClick={() => setSubTab('explore')}>
                Explorar alimentos
              </button>
            </div>
          ) : (
            <div className="inventory-card-grid">
              {myReservations.map((res) => (
                <article key={res.id} className="inventory-card">
                  <div className="card-top-row">
                    <div>
                      <h3 className="card-product-title">{res.foodName}</h3>
                      <span className="card-date-meta">Propietario: <strong>{res.ownerName}</strong></span>
                    </div>
                    <span className={`listing-status-tag ${res.status.toLowerCase()}`}>
                      {res.status === 'ACTIVE' ? 'VIGENTE' : res.status === 'COMPLETED' ? 'COMPLETADA' : 'CANCELADA'}
                    </span>
                  </div>

                  {res.status === 'ACTIVE' && (
                    <div className="pickup-code-display-card">
                      <span className="pickup-code-hint">TU CÓDIGO DE RECOGIDA</span>
                      <div className="pickup-code-digits">{res.pickupCode}</div>
                      <p className="pickup-code-instructions">
                        Muéstrale estos 4 números a {res.ownerName} para que confirme tu entrega.
                      </p>
                      <button
                        className="btn-link text-danger mt-2"
                        onClick={() => handleCancelReservation(res.id)}
                      >
                        Cancelar reserva
                      </button>
                    </div>
                  )}

                  {res.status === 'COMPLETED' && (
                    <div className="alert-banner success mt-2">
                      <CheckCircleIcon size={16} />
                      <span>Alimento recogido con éxito.</span>
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </>
      )}

      {/* 4. MIS PUBLICACIONES */}
      {subTab === 'my-listings' && (
        <>
          {isLoading ? (
            <div className="skeleton-list">
              <div className="skeleton-card" />
            </div>
          ) : myListings.length === 0 ? (
            <div className="clean-empty-box">
              <h4>No has publicado alimentos</h4>
              <p>Puedes publicar lotes desde tu despensa para compartirlos con la comunidad.</p>
              <button className="btn-primary-sm" onClick={() => onNavigate('inventory')}>
                Ir a mi despensa
              </button>
            </div>
          ) : (
            <div className="inventory-card-grid">
              {myListings.map((l) => (
                <article key={l.id} className="inventory-card">
                  <div className="card-top-row">
                    <div>
                      <h3 className="card-product-title">{l.foodName}</h3>
                      <span className="card-date-meta">{l.locationName}</span>
                    </div>
                    <span className={`listing-status-tag ${l.status.toLowerCase()}`}>
                      {l.status === 'ACTIVE' ? 'DISPONIBLE' : l.status === 'RESERVED' ? 'RESERVADO' : 'CERRADO'}
                    </span>
                  </div>
                  <p className="listing-description">{l.description}</p>
                </article>
              ))}
            </div>
          )}
        </>
      )}

      {/* Modal Inmediato al Reservar */}
      {newReservation && (
        <div className="modal-scrim" onClick={() => setNewReservation(null)}>
          <div className="sheet-modal" onClick={(e) => e.stopPropagation()}>
            <div className="sheet-handle" />
            <header className="sheet-header">
              <div>
                <span className="sheet-eyebrow">¡RESERVA CONFIRMADA!</span>
                <h3 className="sheet-title">{newReservation.foodName}</h3>
              </div>
              <button className="btn-close-sheet" onClick={() => setNewReservation(null)}>✕</button>
            </header>
            <div className="sheet-body">
              <div className="pickup-code-display-card">
                <span className="pickup-code-hint">TU CÓDIGO DE RECOGIDA</span>
                <div className="pickup-code-digits">{newReservation.pickupCode}</div>
                <p className="pickup-code-instructions">
                  Propietario: <strong>{newReservation.ownerName}</strong>
                  <br />
                  Guarda este número para dictárselo al recoger el producto.
                </p>
              </div>
            </div>
            <footer className="sheet-footer">
              <button className="btn-primary" onClick={() => setNewReservation(null)}>
                Entendido
              </button>
            </footer>
          </div>
        </div>
      )}
    </div>
  );
};
