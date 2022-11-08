import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './tasks.reducer';

export const TasksDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const tasksEntity = useAppSelector(state => state.tasks.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="tasksDetailsHeading">
          <Translate contentKey="rfidRevampElkApp.tasks.detail.title">Tasks</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{tasksEntity.id}</dd>
          <dt>
            <span id="nameEn">
              <Translate contentKey="rfidRevampElkApp.tasks.nameEn">Name En</Translate>
            </span>
          </dt>
          <dd>{tasksEntity.nameEn}</dd>
          <dt>
            <span id="nameAr">
              <Translate contentKey="rfidRevampElkApp.tasks.nameAr">Name Ar</Translate>
            </span>
          </dt>
          <dd>{tasksEntity.nameAr}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="rfidRevampElkApp.tasks.status">Status</Translate>
            </span>
          </dt>
          <dd>{tasksEntity.status}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="rfidRevampElkApp.tasks.code">Code</Translate>
            </span>
          </dt>
          <dd>{tasksEntity.code}</dd>
        </dl>
        <Button tag={Link} to="/tasks" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/tasks/${tasksEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TasksDetail;
