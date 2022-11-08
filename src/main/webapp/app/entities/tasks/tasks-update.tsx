import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ITasks } from 'app/shared/model/tasks.model';
import { getEntity, updateEntity, createEntity, reset } from './tasks.reducer';

export const TasksUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const tasksEntity = useAppSelector(state => state.tasks.entity);
  const loading = useAppSelector(state => state.tasks.loading);
  const updating = useAppSelector(state => state.tasks.updating);
  const updateSuccess = useAppSelector(state => state.tasks.updateSuccess);

  const handleClose = () => {
    navigate('/tasks' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...tasksEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...tasksEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="rfidRevampElkApp.tasks.home.createOrEditLabel" data-cy="TasksCreateUpdateHeading">
            <Translate contentKey="rfidRevampElkApp.tasks.home.createOrEditLabel">Create or edit a Tasks</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="tasks-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('rfidRevampElkApp.tasks.nameEn')}
                id="tasks-nameEn"
                name="nameEn"
                data-cy="nameEn"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rfidRevampElkApp.tasks.nameAr')}
                id="tasks-nameAr"
                name="nameAr"
                data-cy="nameAr"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('rfidRevampElkApp.tasks.status')}
                id="tasks-status"
                name="status"
                data-cy="status"
                type="text"
              />
              <ValidatedField label={translate('rfidRevampElkApp.tasks.code')} id="tasks-code" name="code" data-cy="code" type="text" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/tasks" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default TasksUpdate;
