/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheAPI} from '../../components/api/che-api.factory';
import {CheWorkspace} from '../../components/api/che-workspace.factory';
import {RouteHistory} from '../../components/routing/route-history.service';
import {CheUIElementsInjectorService} from '../../components/injector/che-ui-elements-injector.service';

/**
 * This class is handling the service for viewing the IDE
 * @author Florent Benoit
 */
class IdeSvc {
  $location: ng.ILocationService;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  $rootScope: ng.IRootScopeService;
  $sce: ng.ISCEService;
  $timeout: ng.ITimeoutService;
  cheAPI: CheAPI;
  cheWorkspace: CheWorkspace;
  lodash: any;
  proxySettings: any;
  routeHistory: RouteHistory;
  userDashboardConfig: any;
  cheUIElementsInjectorService: CheUIElementsInjectorService;

  ideParams: Map<string, string>;
  lastWorkspace: any;
  openedWorkspace: any;

  ideAction: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService,
              $q: ng.IQService, $rootScope: ng.IRootScopeService, $sce: ng.ISCEService, $timeout: ng.ITimeoutService,
              cheAPI: CheAPI, cheWorkspace: CheWorkspace, lodash: any, proxySettings: any, routeHistory: RouteHistory,
              userDashboardConfig: any, cheUIElementsInjectorService: CheUIElementsInjectorService) {
    this.$location = $location;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$rootScope = $rootScope;
    this.$sce = $sce;
    this.$timeout = $timeout;
    this.cheAPI = cheAPI;
    this.cheWorkspace = cheWorkspace;
    this.lodash = lodash;
    this.proxySettings = proxySettings;
    this.routeHistory = routeHistory;
    this.userDashboardConfig = userDashboardConfig;
    this.cheUIElementsInjectorService = cheUIElementsInjectorService;

    this.ideParams = new Map();

    this.lastWorkspace = null;
    this.openedWorkspace = null;
  }

  displayIDE(): void {
    (this.$rootScope as any).showIDE = true;
  }

  restoreIDE(): void {
    (this.$rootScope as any).restoringIDE = true;
    this.displayIDE();
  }

  hasIdeLink(): boolean {
    return (this.$rootScope as any).ideIframeLink && ((this.$rootScope as any).ideIframeLink !== null);
  }

  handleError(error: any): void {
    this.$log.error(error);
  }

  startIde(workspace: any): ng.IPromise<any> {
    this.lastWorkspace = workspace;

    if (this.openedWorkspace && this.openedWorkspace.id === workspace.id) {
      this.openedWorkspace = null;
    }

    this.updateRecentWorkspace(workspace.id);

    let startWorkspaceDefer = this.$q.defer();
    this.startWorkspace(workspace).then(() => {
      // update list of workspaces
      // for new workspace to show in recent workspaces
      this.cheWorkspace.fetchWorkspaces();

      this.cheWorkspace.fetchStatusChange(workspace.id, 'RUNNING').then(() => {
        return this.cheWorkspace.fetchWorkspaceDetails(workspace.id);
      }).then(() => {
        startWorkspaceDefer.resolve();
      }, (error: any) => {
        this.handleError(error);
        startWorkspaceDefer.reject(error);
      });
      this.cheWorkspace.fetchStatusChange(workspace.id, 'ERROR').then((data: any) => {
        startWorkspaceDefer.reject(data);
      });
    }, (error: any) => {
      startWorkspaceDefer.reject(error);
    });

    return startWorkspaceDefer.promise;
  }

  startWorkspace(data: any): ng.IPromise<any> {
    let startWorkspacePromise = this.cheAPI.getWorkspace().startWorkspace(data.id, data.config.defaultEnv);
    return startWorkspacePromise;
  }

  setLoadingParameter(paramName: string, paramValue: string): void {
    this.ideParams.set(paramName, paramValue);
  }

  setIDEAction(ideAction: string): void {
    this.ideAction = ideAction;
  }

  openIde(workspaceId: string): void {
    (this.$rootScope as any).hideNavbar = false;

    this.updateRecentWorkspace(workspaceId);

    let inDevMode = this.userDashboardConfig.developmentMode;
    let randVal = Math.floor((Math.random() * 1000000) + 1);
    let appendUrl = '?uid=' + randVal;

    let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    this.openedWorkspace = workspace;

    let ideUrlLink = this.getHrefLink(workspace, 'ide url');

    if (this.ideAction != null) {
      appendUrl = appendUrl + '&action=' + this.ideAction;

      // reset action
      this.ideAction = null;
    }

    if (this.ideParams) {
      for (let [key, val] of this.ideParams) {
        appendUrl = appendUrl + '&' + key + '=' + val;
      }
      this.ideParams.clear();
    }

    // perform remove of iframes in parent node. It's needed to avoid any script execution (canceled requests) on iframe source changes.
    let iframeParent = angular.element('#ide-application-frame');
    iframeParent.find('iframe').remove();

    if (inDevMode) {
      (this.$rootScope as any).ideIframeLink = this.$sce.trustAsResourceUrl(ideUrlLink + appendUrl);
    } else {
      (this.$rootScope as any).ideIframeLink = ideUrlLink + appendUrl;
    }

    // iframe element for IDE application:
    let iframeElement = '<iframe class=\"ide-page-frame\" id=\"ide-application-iframe\" ng-src=\"{{ideIframeLink}}\" ></iframe>';
    this.cheUIElementsInjectorService.injectAdditionalElement(iframeParent, iframeElement);

    let defer = this.$q.defer();
    if (workspace.status === 'RUNNING') {
      defer.resolve();
    } else {
      this.cheWorkspace.fetchStatusChange(workspace.id, 'STARTING').then(() => {
        defer.resolve();
      }, (error: any) => {
        defer.reject(error);
        this.$log.error('Unable to start workspace: ', error);
      });
    }
    defer.promise.then(() => {
      // update list of recent workspaces
      this.cheWorkspace.fetchWorkspaces();
    });
  }

  /**
   * Gets link from a workspace
   * @param workspace the workspace on which analyze the links
   * @param name the name of the link to find (rel attribute)
   * @returns empty or the href attribute of the link
   */
  getHrefLink(workspace: any, name: string): string {
    let links = workspace.links;
    let i = 0;
    while (i < links.length) {
      let link = links[i];
      if (link.rel === name) {
        return link.href;
      }
      i++;
    }
    return '';
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list
   *
   * @param workspaceId
   */
  updateRecentWorkspace(workspaceId: string): void {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }
}

export default IdeSvc;
