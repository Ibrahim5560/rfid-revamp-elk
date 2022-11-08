import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Tasks from './tasks';
import TasksDetail from './tasks-detail';
import TasksUpdate from './tasks-update';
import TasksDeleteDialog from './tasks-delete-dialog';

const TasksRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Tasks />} />
    <Route path="new" element={<TasksUpdate />} />
    <Route path=":id">
      <Route index element={<TasksDetail />} />
      <Route path="edit" element={<TasksUpdate />} />
      <Route path="delete" element={<TasksDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TasksRoutes;
