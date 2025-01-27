import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from '../service/constant.service';
import { ChangeInfo } from '../interface/change-info';
import { ProjectInfoModel } from '../interface/project-info';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  getProjectList(): Observable<ProjectInfoModel[]> {
    return this.http.get<ProjectInfoModel[]>(
      `${this.baseUrl}/get-project-list`
    );
  }

  getChangesOfProject(projectId: string): Observable<ChangeInfo[]> {
    return this.http.get<ChangeInfo[]>(
      `${this.baseUrl}/get-changes?q=project:${projectId}`
    );
  }

  // getAnonymousCommitList(): Observable<ChangeInfo[]> {
  //   return this.http.get<ChangeInfo[]>(
  //     `${this.baseUrl}/get-anonymous-commit-list`
  //   );
  // }
}
