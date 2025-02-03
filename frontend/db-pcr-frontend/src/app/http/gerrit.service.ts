import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from '../service/constant.service';
import { ChangeInfo } from '../interface/change-info';
import { ProjectInfoModel } from '../interface/project-info';
import { ModiFileInfo } from '../interface/modi-file-info';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  getProjectList(): Observable<Map<string, ProjectInfoModel>> {
    return this.http.get<Map<string, ProjectInfoModel>>(
      `${this.baseUrl}/get-project-list`
    );
  }

  getChangesOfProject(projectId: string): Observable<ChangeInfo[]> {
    return this.http.get<ChangeInfo[]>(
      `${this.baseUrl}/get-changes?q=project:${projectId}`
    );
  }

  getModifiedFileInChange(
    changeId: string,
    revisionId: string
  ): Observable<Map<string, ModiFileInfo>> {
    return this.http.get<Map<string, ModiFileInfo>>(
      `${this.baseUrl}/get-modified-file-list?changeId=${changeId}&revisionId=${revisionId}`
    );
  }
}
