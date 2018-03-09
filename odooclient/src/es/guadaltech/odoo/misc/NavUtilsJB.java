/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.guadaltech.odoo.misc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;

/* En la otra clase (NavUtils) se especifica que esta clase se usará únicamente
 * cuando corresponda, es decir, con API 16 o superior, por lo que es seguro
 * ignorar las advertencias 
 */

class NavUtilsJB {
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static Intent getParentActivityIntent(Activity activity) {
		return activity.getParentActivityIntent();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static boolean shouldUpRecreateTask(Activity activity, Intent targetIntent) {
		return activity.shouldUpRecreateTask(targetIntent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void navigateUpTo(Activity activity, Intent upIntent) {
		activity.navigateUpTo(upIntent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static String getParentActivityName(ActivityInfo info) {
		return info.parentActivityName;
	}
}