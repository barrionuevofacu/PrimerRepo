using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Threading.Tasks;
using WebApiObjetos.Data;
using WebApiObjetos.Models.Entities;
using WebApiObjetos.Models.Repositories.Interfaces;

namespace WebApiObjetos.Models.Repositories
{
    public class RepositoryBase<TEntity> : IRepository<TEntity> where TEntity : class
    {
        protected DbContext applicationDbContext;

        public RepositoryBase(DbContext dbContext)
        {
            this.applicationDbContext = dbContext;
        }


        public async Task<TEntity> Add(TEntity entity)
        {
            try
            {
                applicationDbContext.Add<TEntity>(entity);
                await applicationDbContext.SaveChangesAsync();
                return entity;
            }
            catch (Exception e)
            {
                throw e;
            }
        }


        public async Task<bool> DeleteById(int id)
        {
            var entityToDelete = GetById(id);
            
            if (entityToDelete != null)
            {
                applicationDbContext.Remove(entityToDelete);
                await applicationDbContext.SaveChangesAsync();
                return true;
            }
            return false;
        }

        public async Task Delete(TEntity entity)
        {
            try
            {
                applicationDbContext.Remove<TEntity>(entity);
                await applicationDbContext.SaveChangesAsync();
            }
            catch (Exception e)
            {
                throw e;
            }
        }

        public async Task<IEnumerable<TEntity>> GetAll()
        {
            return await applicationDbContext.Set<TEntity>().ToListAsync();
        }

        public async Task Update(TEntity entity)
        {
            try
            {
                applicationDbContext.Update(entity);
                await applicationDbContext.SaveChangesAsync();
            }
            catch (Exception e) { }
        }

        public async Task<TEntity> GetById(int id)
        {
            return await applicationDbContext.FindAsync<TEntity>(id);
        }

        public async Task<IList<TEntity>> FindBy(Expression<Func<TEntity, bool>> predicate) /// método base para busqueda dada una expresión
        {
            IList<TEntity> query = await applicationDbContext.Set<TEntity>().Where(predicate).ToListAsync();
            return query;
        }

        public async Task<bool> Any(Expression<Func<TEntity, bool>> predicate)
        {
            return await applicationDbContext.Set<TEntity>().AnyAsync(predicate);
        }

    }
}

