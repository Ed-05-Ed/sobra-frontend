import React, { useState, useEffect } from 'react';
import { FoodItem, DateType, User } from '../types/api';
import { api } from '../api/endpoints';
import { CustomApiError } from '../api/client';
import { AlertCircleIcon, CheckCircleIcon } from '../components/Icons';

interface Props {
  currentUser: User;
  editingItem?: FoodItem | null;
  onNavigate: (view: any) => void;
}

export const FoodFormPage: React.FC<Props> = ({ currentUser, editingItem, onNavigate }) => {
  const isEditing = !!editingItem;

  const [ingredients, setIngredients] = useState<any[]>([]);
  const [selectedIngredientId, setSelectedIngredientId] = useState(editingItem?.ingredientId || '');
  const [name, setName] = useState(editingItem?.name || '');
  const [quantity, setQuantity] = useState(editingItem?.remainingQuantity?.toString() || '');
  const [labelDate, setLabelDate] = useState(editingItem?.labelDate || '');
  const [dateType, setDateType] = useState<DateType>(editingItem?.dateType || 'EXPIRATION');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    api.getIngredients()
      .then((data: any[]) => {
        setIngredients(data);
        if (!isEditing && data.length > 0 && !selectedIngredientId) {
          const firstId = data[0].id || data[0].ingredientId;
          setSelectedIngredientId(firstId || '');
          if (!name && data[0].name) {
            setName(data[0].name);
          }
        }
      })
      .catch((err) => console.error('Error al cargar ingredientes:', err));
  }, [isEditing]);

  const handleIngredientChange = (ingId: string) => {
    setSelectedIngredientId(ingId);
    const found = ingredients.find((i: any) => (i.id || i.ingredientId) === ingId);
    if (found && !isEditing) {
      setName(found.name);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setSuccessMsg(null);

    const resolvedUserId = currentUser.id || (currentUser as any).userId;

    if (!isEditing && !selectedIngredientId) {
      setFormError('Por favor selecciona un ingrediente del catálogo.');
      return;
    }

    if (!name.trim()) {
      setFormError('El nombre del alimento es obligatorio.');
      return;
    }
    if (!labelDate) {
      setFormError('Ingresa la fecha indicada en la etiqueta.');
      return;
    }

    if (!isEditing) {
      const numQty = parseFloat(quantity);
      if (isNaN(numQty) || numQty <= 0) {
        setFormError('La cantidad inicial debe ser mayor a cero.');
        return;
      }

      // Estructura exacta requerida por el backend en Sección 8 (sin el campo unit)
      const payload = {
        userId: resolvedUserId,
        ingredientId: selectedIngredientId,
        name: name.trim(),
        quantity: numQty,
        labelDate: labelDate,
        dateType: 'EXPIRATION',
      };

      console.log('Enviando POST /api/foods:', payload);

      setIsSubmitting(true);
      try {
        await api.createFood(payload);
        setSuccessMsg('Alimento registrado con éxito.');
        setTimeout(() => onNavigate('inventory'), 600);
      } catch (err: any) {
        console.error('Error del backend:', err);
        let msg = err.message || 'Error al registrar.';
        if (err.details) {
          msg += ` (${typeof err.details === 'object' ? JSON.stringify(err.details) : err.details})`;
        }
        setFormError(msg);
      } finally {
        setIsSubmitting(false);
      }
    } else {
      setIsSubmitting(true);
      try {
        await api.updateFood(editingItem.id, {
          name: name.trim(),
          labelDate,
          dateType: 'EXPIRATION',
          version: editingItem.version,
        });
        setSuccessMsg('Metadatos actualizados.');
        setTimeout(() => onNavigate('inventory'), 600);
      } catch (err: any) {
        if (err instanceof CustomApiError && err.status === 409) {
          setFormError('Conflicto (409): Otro usuario modificó este lote.');
        } else {
          setFormError(err.message || 'Error al actualizar.');
        }
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  return (
    <div className="view-wrapper">
      <header className="page-nav-header">
        <div>
          <span className="hero-tag">USUARIO: {currentUser.name.toUpperCase()}</span>
          <h1 className="hero-headline">{isEditing ? 'Modificar datos' : 'Nuevo Alimento'}</h1>
        </div>
        <button className="btn-ghost-sm" onClick={() => onNavigate('inventory')}>
          Volver
        </button>
      </header>

      {formError && (
        <div className="alert-banner error" role="alert">
          <AlertCircleIcon size={16} />
          <span>{formError}</span>
        </div>
      )}

      {successMsg && (
        <div className="alert-banner success" role="status">
          <CheckCircleIcon size={16} />
          <span>{successMsg}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="custom-form-card">
        {!isEditing && (
          <div className="form-field">
            <label className="field-label" htmlFor="catSelect">Ingrediente en catálogo</label>
            <div className="select-wrapper">
              <select
                id="catSelect"
                value={selectedIngredientId}
                onChange={(e) => handleIngredientChange(e.target.value)}
              >
                {ingredients.map((ing: any) => {
                  const id = ing.id || ing.ingredientId;
                  return (
                    <option key={id} value={id}>
                      {ing.name}
                    </option>
                  );
                })}
              </select>
            </div>
            <span className="field-caption">La unidad se determina automáticamente según el catálogo.</span>
          </div>
        )}

        <div className="form-field">
          <label className="field-label" htmlFor="prodName">Nombre específico</label>
          <input
            id="prodName"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Ej. Pan integral Bimbo, Leche Santa Clara"
            required
          />
        </div>

        {!isEditing ? (
          <div className="form-field">
            <label className="field-label" htmlFor="initQty">Cantidad</label>
            <input
              id="initQty"
              type="number"
              step="any"
              min="0.01"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="Ej. 250, 500, 10"
              required
            />
          </div>
        ) : (
          <div className="stock-summary-card">
            <div>
              <span className="stock-meta-label">Saldo en sistema</span>
              <strong className="stock-meta-value">{editingItem.remainingQuantity}</strong>
            </div>
            <small className="field-caption">El saldo sólo se altera registrando consumos o retiros.</small>
          </div>
        )}

        <div className="form-field">
          <label className="field-label">Criterio de fecha</label>
          <div className="segmented-selector">
            <button
              type="button"
              className={`segmented-option ${dateType === 'EXPIRATION' ? 'selected' : ''}`}
              onClick={() => setDateType('EXPIRATION')}
            >
              Caducidad legal
            </button>
            <button
              type="button"
              className={`segmented-option ${dateType === 'BEST_BEFORE' ? 'selected' : ''}`}
              onClick={() => setDateType('BEST_BEFORE')}
            >
              Consumo preferente
            </button>
          </div>

          <div className="mt-2">
            <input
              type="date"
              value={labelDate}
              onChange={(e) => setLabelDate(e.target.value)}
              required
            />
          </div>
        </div>

        <div className="form-submit-row">
          <button type="submit" className="btn-primary-large" disabled={isSubmitting}>
            {isSubmitting ? 'Procesando...' : isEditing ? 'Guardar Modificaciones' : 'Confirmar Ingreso'}
          </button>
        </div>
      </form>
    </div>
  );
};