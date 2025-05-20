import { Injectable } from '@angular/core';
import { SPRING_URL } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { ProjectDto } from '../interface/database/project-dto';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  /**  Get user's project list */
  getProjects(): Observable<ProjectDto[]> {
    return this.http.get<ProjectDto[]>(`${this.baseUrl}/projects`, {
      withCredentials: true,
    });
  }

  /** Get project by group */
  getGroupProjects(): Observable<ProjectDto[]> {
    return this.http.get<ProjectDto[]>(`${this.baseUrl}/group-projects`, {
      withCredentials: true,
    });
  }
}
