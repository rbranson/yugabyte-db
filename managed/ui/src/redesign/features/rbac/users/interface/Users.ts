/*
 * Created on Mon Jul 31 2023
 *
 * Copyright 2021 YugaByte, Inc. and Contributors
 * Licensed under the Polyform Free Trial License 1.0.0 (the "License")
 * You may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://github.com/YugaByte/yugabyte-db/blob/master/licenses/POLYFORM-FREE-TRIAL-LICENSE-1.0.0.txt
 */

import { UniverseResource } from '../../policy/IPolicy';

export interface RbacUser {
  uuid: string;
  email: string;
  password?: string;
  confirmPassword?: string;
  creationDate?: string;
}

export interface RbacUserWithResources extends Partial<RbacUser> {
  roleResourceDefinitions?: {
    roleUUID: string;
    resourceGroup: {
      resourceDefinitionSet: UniverseResource[];
    };
  }[];
}
