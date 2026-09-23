import React, { useState } from 'react';
import { FoodItem, ListingType } from '../types/api';
import { api } from '../api/endpoints';

interface Props {
  food: FoodItem | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const PublishModal: React.FC<Props> = ({ food, onClose, onSuccess }) => {
  if (!food) return null;

  const [type, setType] = useState<ListingType>('DONATION');
  const [price, setPrice] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [locationName, setLocationName] = useState<string>('Santiago Tianguistenco');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    let parsedPrice: number | null = null;
    if (type === 'SALE') {
      parsedPrice = parseFloat(price);
      if (isNaN(parsedPrice) || parsedPrice <= 0) {
        setErrorMessage('Para una venta, el precio debe ser mayor a cero.');
        return;
      }
    }

    setIsSubmitting(true);
    try {
      await api.createListing({
        foodId: food.id,
        type,
        price: parsedPrice,
        description: description.trim() || (type === 'DONATION' ? 'Donación de despensa para evitar desperdicio' : 'Alimento disponible'),
        latitude: 19.1812,
        longitude: -99.4683,
        locationName: locationName.trim(),
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Error al crear la publicación en el Marketplace.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-scrim" onClick={onClose}>
      <div className="sheet-modal" onClick={(e) => e.stopPropagation()}>
        <div className="sheet-handle" />
        <header className="sheet-header">
          <div>
            <span className="sheet-eyebrow">PUBLICAR EN COMUNIDAD</span>
            <h3 className="sheet-title">{food.name}</h3>
          </div>
          <button className="btn-close-sheet" onClick={onClose}>✕</button>
        </header>

        <form onSubmit={handleSubmit}>
          <div className="sheet-body">
            <div className="form-field">
              <label className="field-label">Modalidad de publicación</label>
              <div className="segmented-selector">
                <button
                  type="button"
                  className={`segmented-option ${type === 'DONATION' ? 'selected' : ''}`}
                  onClick={() => setType('DONATION')}
                >
                  Donación solidaria
                </button>
                <button
                  type="button"
                  className={`segmented-option ${type === 'SALE' ? 'selected' : ''}`}
                  onClick={() => setType('SALE')}
                >
                  Venta a bajo costo
                </button>
              </div>
            </div>

            {type === 'SALE' && (
              <div className="form-field">
                <label className="field-label" htmlFor="priceInput">Precio de venta ($ MXN)</label>
                <input
                  id="priceInput"
                  type="number"
                  step="0.50"
                  min="1"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="Ej: 25.00"
                  required
                />
              </div>
            )}

            <div className="form-field">
              <label className="field-label" htmlFor="descInput">Descripción / Estado del alimento</label>
              <input
                id="descInput"
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Ej. Paquete sellado, consumo antes del viernes"
              />
            </div>

            <div className="form-field">
              <label className="field-label" htmlFor="locInput">Punto de recogida</label>
              <input
                id="locInput"
                type="text"
                value={locationName}
                onChange={(e) => setLocationName(e.target.value)}
                placeholder="Ej. Santiago Tianguistenco, Centro"
              />
              <span className="field-caption">Coordenadas demo: 19.1812, -99.4683</span>
            </div>

            {errorMessage && (
              <div className="alert-banner error" role="alert">
                <span>{errorMessage}</span>
              </div>
            )}
          </div>

          <footer className="sheet-footer">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSubmitting}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Publicando...' : 'Publicar en Marketplace'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
};
