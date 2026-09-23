import React, { useState } from 'react';
import { FoodItem, MovementType } from '../types/api';
import { api } from '../api/endpoints';
import { CustomApiError } from '../api/client';
import { AlertCircleIcon, CheckCircleIcon, TrashIcon } from './Icons';

interface Props {
  food: FoodItem | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const MovementModal: React.FC<Props> = ({ food, onClose, onSuccess }) => {
  if (!food) return null;

  const [type, setType] = useState<MovementType>('CONSUMED');
  const [quantity, setQuantity] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [operationId] = useState<string>(() => crypto.randomUUID());

  const applyFraction = (factor: number) => {
    const rawVal = food.remainingQuantity * factor;
    if (food.unit === 'PIECE') {
      const val = Math.max(1, Math.round(rawVal));
      setQuantity(val.toString());
    } else {
      const val = parseFloat(rawVal.toFixed(2));
      setQuantity(val.toString());
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    const numQty = parseFloat(quantity);
    if (isNaN(numQty) || numQty <= 0) {
      setErrorMessage('Ingresa una cantidad mayor a 0');
      return;
    }

    if (food.unit === 'PIECE' && !Number.isInteger(numQty)) {
      setErrorMessage('Para unidades en piezas, la cantidad debe ser un entero.');
      return;
    }

    if (numQty > food.remainingQuantity) {
      setErrorMessage(`Excede el saldo actual (${food.remainingQuantity} ${food.unit})`);
      return;
    }

    setIsSubmitting(true);
    try {
      await api.registerMovement(food.id, {
        operationId,
        type,
        quantity: numQty,
        expectedVersion: food.version,
      });
      onSuccess();
      onClose();
    } catch (err) {
      if (err instanceof CustomApiError && err.status === 409) {
        setErrorMessage('Conflicto (409): El saldo fue modificado simultáneamente. Actualiza para ver el valor más reciente.');
      } else if (err instanceof CustomApiError) {
        setErrorMessage(err.message || 'Error al registrar el movimiento.');
      } else {
        setErrorMessage('No hay conexión con el servidor. Tu operación no se duplicará al reintentar.');
      }
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
            <span className="sheet-eyebrow">REGISTRO DE CONSUMO</span>
            <h3 className="sheet-title">{food.name}</h3>
          </div>
          <button className="btn-close-sheet" onClick={onClose} aria-label="Cerrar">✕</button>
        </header>

        <form onSubmit={handleSubmit}>
          <div className="sheet-body">
            <div className="stock-summary-card">
              <div className="stock-meta">
                <span className="stock-meta-label">Disponible en despensa</span>
                <span className="stock-meta-value">{food.remainingQuantity} <small>{food.unit}</small></span>
              </div>
              <div className="stock-date">
                <span>{food.labelDate}</span>
                <small>{food.dateType === 'EXPIRATION' ? 'Caducidad' : 'Cons. Pref.'}</small>
              </div>
            </div>

            <div className="form-field">
              <label className="field-label">Destino del alimento</label>
              <div className="segmented-selector">
                <button
                  type="button"
                  className={`segmented-option ${type === 'CONSUMED' ? 'selected' : ''}`}
                  onClick={() => setType('CONSUMED')}
                >
                  <CheckCircleIcon size={16} />
                  <span>Aprovechado</span>
                </button>
                <button
                  type="button"
                  className={`segmented-option danger ${type === 'WASTED' ? 'selected' : ''}`}
                  onClick={() => setType('WASTED')}
                >
                  <TrashIcon size={16} />
                  <span>Desperdicio</span>
                </button>
              </div>
            </div>

            <div className="form-field">
              <div className="label-with-presets">
                <label className="field-label" htmlFor="qtyInput">Cantidad utilizada ({food.unit})</label>
                <div className="presets-row">
                  <button type="button" onClick={() => applyFraction(0.25)}>25%</button>
                  <button type="button" onClick={() => applyFraction(0.50)}>50%</button>
                  <button type="button" onClick={() => applyFraction(1.00)}>Todo</button>
                </div>
              </div>
              <div className="input-with-suffix">
                <input
                  id="qtyInput"
                  type="number"
                  step={food.unit === 'PIECE' ? '1' : '0.01'}
                  min={food.unit === 'PIECE' ? '1' : '0.01'}
                  max={food.remainingQuantity}
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  placeholder="0.00"
                  autoFocus
                  required
                />
                <span className="input-suffix">{food.unit}</span>
              </div>
            </div>

            {errorMessage && (
              <div className="alert-banner error" role="alert">
                <AlertCircleIcon size={16} />
                <span>{errorMessage}</span>
              </div>
            )}
          </div>

          <footer className="sheet-footer">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSubmitting}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Confirmando...' : 'Confirmar registro'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
};
